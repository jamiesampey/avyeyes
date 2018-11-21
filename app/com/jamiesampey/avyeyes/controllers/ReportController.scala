package com.jamiesampey.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.jamiesampey.avyeyes.data.CachedDao
import com.jamiesampey.avyeyes.model.Avalanche
import com.jamiesampey.avyeyes.service.AvyEyesUserService.AdminRoles
import com.jamiesampey.avyeyes.service.{AmazonS3Service, ConfigurationService, ExternalIdService}
import com.jamiesampey.avyeyes.system.UserEnvironment
import com.jamiesampey.avyeyes.util.FutureOps._
import org.joda.time.DateTime
import org.json4s.JsonAST._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Request}
import play.api.libs.mailer._
import securesocial.core.SecureSocial

import scala.util.{Failure, Success, Try}

@Singleton
class ReportController @Inject()(implicit val dao: CachedDao, idService: ExternalIdService, s3: AmazonS3Service,
                                 val configService: ConfigurationService, val log: Logger, mailerClient: MailerClient,
                                 authorizations: Authorizations, implicit val env: UserEnvironment, val messagesApi: MessagesApi)
  extends SecureSocial with Json4sMethods with I18nSupport {

  import authorizations._

  def newReportId = Action { implicit request => Try(idService.reserveNewExtId) match {
    case Success(newExtId) =>
      log.info(s"Served extId request from ${request.remoteAddress} with new extId $newExtId")
      Ok(writeJson(JObject(List(JField("extId", JString(newExtId))))))
    case Failure(ex) =>
      log.error(s"Exception thrown while serving extId request from ${request.remoteAddress}", ex)
      InternalServerError
  }}

  def submitReport(extId: String) = Action(parse.tolerantText) { implicit request => parseAvalancheFromRequest match {
    case Success(avalancheFromData) =>
      log.info(s"Successfully parsed new avalanche $extId from report data. Validating fields")

      val now = DateTime.now
      val avalanche = avalancheFromData.copy(createTime = now, updateTime = now, viewable = true)

      dao.insertAvalanche(avalanche)
      s3.allowPublicFileAccess(extId)
      sendSubmissionEmails(avalanche)
      idService.unreserveExtId(extId)

      log.info(s"Avalanche $extId successfully inserted")
      Ok(extId)

    case Failure(ex) =>
      log.error(s"Unable to deserialize avalanche from report $extId", ex)
      BadRequest
  }}

  def updateReport(extId: String, editKeyOpt: Option[String]) = UserAwareAction(parse.tolerantText) { implicit request => parseAvalancheFromRequest match {
    case Success(avalancheFromData) =>
      log.info(s"Successfully parsed updated avalanche $extId from report data. Validating fields")
      dao.getAvalanche(extId) match {
        case Some(existingAvalanche) if isAuthorizedToEdit(extId, request.user, editKeyOpt) =>
          val viewableUpdate = if (isAdmin(request.user)) avalancheFromData.viewable else existingAvalanche.viewable
          log.debug(s"Setting viewable=$viewableUpdate for avalanche $extId")
          dao.updateAvalanche(avalancheFromData.copy(createTime = existingAvalanche.createTime, viewable = viewableUpdate))
          if (viewableUpdate) s3.allowPublicImageAccess(extId) else s3.denyPublicImageAccess(extId)
          log.info(s"Avalanche $extId successfully updated")
          Ok
        case Some(_) =>
          log.warn(s"Received update request for $extId but edit is not authorized")
          Unauthorized
        case _ =>
          log.error(s"Received update request for non-existent avalanche")
          InternalServerError
      }
    case Failure(ex) =>
      log.error(s"Unable to deserialize avalanche from report $extId", ex)
      BadRequest
  }}

  def deleteReport(extId: String) = SecuredAction(WithRole(AdminRoles)) { implicit request => Try {
    dao.deleteAvalanche(extId).resolve
    s3.deleteAllFiles(extId)
  } match {
    case Success(_) => log.info(s"Avalanche $extId deleted"); Ok
    case Failure(ex) =>
      log.error(s"Error deleting avalanche $extId", ex)
      InternalServerError
  }}

  private def parseAvalancheFromRequest(implicit request: Request[String]): Try[Avalanche] = Try(readJson(Some(request.body)).extract[Avalanche])

  private def sendSubmissionEmails(a: Avalanche) = {
    val emailToAdmin = Email(
      Messages("help.avyReportSubmitEmailAdminSubject", a.submitterEmail), "AvyEyes <avyeyes@gmail.com>", Seq("avyeyes@gmail.com"),
      bodyHtml = Some(Messages("help.avyReportSubmitEmailAdminBody", a.submitterEmail, a.extId, a.title, a.url))
    )

    val emailToSubmitter = Email(
      Messages("help.avyReportSubmitEmailSubmitterSubject", a.title), "AvyEyes <avyeyes@gmail.com>", Seq(a.submitterEmail),
      bodyHtml = Some(Messages("help.avyReportSubmitEmailSubmitterBody", a.title, a.url, a.editUrl))
    )

    mailerClient.send(emailToAdmin)
    mailerClient.send(emailToSubmitter)
  }
}

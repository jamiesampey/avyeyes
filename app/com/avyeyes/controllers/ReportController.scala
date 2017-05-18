package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.Avalanche
import com.avyeyes.service.AvyEyesUserService.AdminRoles
import com.avyeyes.service.{AmazonS3Service, ConfigurationService, ExternalIdService}
import com.avyeyes.system.UserEnvironment
import com.avyeyes.util.FutureOps._
import org.joda.time.DateTime
import org.json4s.JsonAST._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Request}
import play.api.libs.mailer._
import securesocial.core.SecureSocial

import scala.util.{Failure, Success, Try}

@Singleton
class ReportController @Inject()(implicit val dal: CachedDAL, idService: ExternalIdService, s3: AmazonS3Service,
                                 val configService: ConfigurationService, val logger: Logger, mailerClient: MailerClient,
                                 implicit val env: UserEnvironment, val messagesApi: MessagesApi)
  extends SecureSocial with Json4sMethods with I18nSupport {

  def newReportId = Action { implicit request => Try(idService.reserveNewExtId) match {
    case Success(newExtId) =>
      logger.info(s"Served extId request from ${request.remoteAddress} with new extId $newExtId")
      Ok(writeJson(JObject(List(JField("extId", JString(newExtId))))))
    case Failure(ex) =>
      logger.error(s"Exception thrown while serving extId request from ${request.remoteAddress}", ex)
      InternalServerError
  }}

  def submitReport(extId: String) = Action(parse.tolerantText) { implicit request => parseAvalancheFromRequest match {
    case Success(avalancheFromData) =>
      logger.info(s"Successfully parsed new avalanche $extId from report data. Validating fields")

      val now = DateTime.now
      val avalanche = avalancheFromData.copy(createTime = now, updateTime = now, viewable = true)
      dal.insertAvalanche(avalanche)
      s3.allowPublicFileAccess(extId)
      sendSubmissionEmails(avalanche)

      logger.info(s"Avalanche $extId successfully inserted")
      Ok(configService.avalancheUrl(extId))

    case Failure(ex) =>
      logger.error(s"Unable to deserialize avalanche from report $extId", ex)
      BadRequest
  }}

  def updateReport(extId: String, editKeyOpt: Option[String]) = UserAwareAction(parse.tolerantText) { implicit request => parseAvalancheFromRequest match {
    case Success(avalancheFromData) =>
      logger.info(s"Successfully parsed updated avalanche $extId from report data. Validating fields")
      dal.getAvalanche(extId) match {
        case Some(existingAvalanche) =>
          dal.updateAvalanche(avalancheFromData.copy(createTime = existingAvalanche.createTime))
          if (avalancheFromData.viewable) s3.allowPublicImageAccess(extId) else s3.denyPublicImageAccess(extId)
          logger.info(s"Avalanche $extId successfully updated")
          Ok
        case _ =>
          logger.error(s"Received update request for non-existent avalanche")
          InternalServerError
      }
    case Failure(ex) =>
      logger.error(s"Unable to deserialize avalanche from report $extId", ex)
      BadRequest
  }}

  def deleteReport(extId: String) = SecuredAction(WithRole(AdminRoles)) { implicit request => Try {
    dal.deleteAvalanche(extId).resolve
    s3.deleteAllFiles(extId)
  } match {
    case Success(_) => logger.info(s"Avalanche $extId deleted"); Ok
    case Failure(ex) =>
      logger.error(s"Error deleting avalanche $extId", ex)
      InternalServerError
  }}

  private def parseAvalancheFromRequest(implicit request: Request[String]): Try[Avalanche] = Try(readJson(Some(request.body)).extract[Avalanche])

  private def sendSubmissionEmails(a: Avalanche) = {
    val emailToAdmin = Email(
      Messages("msg.avyReportSubmitEmailAdminSubject", a.submitterEmail), "AvyEyes <avyeyes@gmail.com>", Seq("avyeyes@gmail.com"),
      bodyHtml = Some(Messages("msg.avyReportSubmitEmailAdminBody", a.submitterEmail, a.extId, a.title, configService.avalancheUrl(a.extId)))
    )

    val emailToSubmitter = Email(
      Messages("msg.avyReportSubmitEmailSubmitterSubject", a.title), "AvyEyes <avyeyes@gmail.com>", Seq(a.submitterEmail),
      bodyHtml = Some(Messages("msg.avyReportSubmitEmailSubmitterBody", a.title, configService.avalancheUrl(a.extId), configService.avalancheEditUrl(a)))
    )

    mailerClient.send(emailToAdmin)
    mailerClient.send(emailToSubmitter)
  }
}

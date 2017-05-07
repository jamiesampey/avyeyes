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
import play.api.mvc.{Action, Request}
import securesocial.core.SecureSocial

import scala.util.{Failure, Success, Try}

@Singleton
class ReportController @Inject()(implicit val dal: CachedDAL, idService: ExternalIdService, s3: AmazonS3Service, val configService: ConfigurationService, val logger: Logger, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods {

  def newReportId = Action { implicit request => Try(idService.reserveNewExtId) match {
    case Success(newExtId) =>
      logger.info(s"Served extId request from ${request.remoteAddress} with new extId $newExtId")
      Ok(writeJson(JObject(List(JField("extId", JString(newExtId))))))
    case Failure(ex) =>
      logger.error(s"Exception thrown while serving extId request from ${request.remoteAddress}", ex)
      InternalServerError
  }}

  def newReport(extId: String) = Action(parse.tolerantText) { implicit request => parseAvalancheFromRequest match {
    case Success(avalancheFromData) =>
      logger.info(s"Successfully parsed avalanche $extId from report data")
      val now = DateTime.now
      val avalanche = avalancheFromData.copy(createTime = now, updateTime = now, viewable = true)

      Ok
    case Failure(ex) =>
      logger.error(s"Unable to deserialize avalanche from report $extId", ex)
      BadRequest
  }}

  def updateReport(extId: String, editKeyOpt: Option[String]) = UserAwareAction(parse.tolerantText) { implicit request => parseAvalancheFromRequest match {
    case Success(avalancheFromData) =>
      logger.info(s"Successfully parsed avalanche $extId from report data")
      Ok
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
}

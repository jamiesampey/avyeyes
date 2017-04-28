package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.{AdminAvalancheQuery, AvalancheQuery, CachedDAL}
import com.avyeyes.service.AvyEyesUserService.AdminRoles
import com.avyeyes.service.{ConfigurationService, ExternalIdService}
import com.avyeyes.system.UserEnvironment
import org.json4s.JsonAST._
import play.api.Logger
import play.api.mvc.Action
import securesocial.core.SecureSocial

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Singleton
class AvalancheController @Inject()(idService: ExternalIdService, val configService: ConfigurationService, val logger: Logger, implicit val dal: CachedDAL, authorizations: Authorizations, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods {

  import authorizations._

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def newAvalancheId() = Action { implicit request => try {
      val newExtId = idService.reserveNewExtId
      logger.info(s"Served extId request from ${request.remoteAddress} with new extId $newExtId")
      Ok(writeJson(JObject(List(JField("extId", JString(newExtId))))))
    } catch {
      case rte: RuntimeException => {
        logger.error(s"Exception thrown while serving extId request from ${request.remoteAddress}", rte)
        InternalServerError
      }
  }}

  def find(extId: String) = UserAwareAction { implicit request =>
    logger.debug(s"finding avalanche $extId")
    dal.getAvalanche(extId) match {
      case Some(avalanche) => Ok(writeJson(avalancheInitViewData(avalanche)))
      case _ => NotFound
    }
  }

  def details(extId: String, editKeyOpt: Option[String]) = UserAwareAction.async { implicit request =>
    logger.debug(s"serving avalanche details $extId")

    for {
      avalancheOption <- dal.getAvalancheFromDisk(extId)
      images <- dal.getAvalancheImages(extId)
    } yield {

      avalancheOption match {
        case Some(avalanche) if isAuthorizedToEdit(extId, request.user, editKeyOpt) => Ok(writeJson(avalancheReadWriteData(avalanche, images)))
        case Some(avalanche) if isAuthorizedToView(extId, request.user) => Ok(writeJson(avalancheReadOnlyData(avalanche, images)))
        case _ => NotFound
      }
    }
  }

  def search(query: AvalancheQuery) = Action { implicit request =>
    logger.debug(s"searching in ${query.geoBounds}")
    Ok
  }

  def table(query: AdminAvalancheQuery) = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Try(dal.getAvalanchesAdmin(query)) match {
      case Success(result) => Ok(writeAdminTableJson(result))
      case Failure(ex) =>
        logger.error("Failed to retrieve avalanche avalanches for admin table", ex)
        InternalServerError
    }
  }
}

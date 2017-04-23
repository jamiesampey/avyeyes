package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.service.{ConfigurationService, ExternalIdService}
import com.avyeyes.system.UserEnvironment
import org.json4s.JsonAST._
import play.api.Logger
import play.api.mvc.Action
import securesocial.core.SecureSocial

import scala.concurrent.ExecutionContext

@Singleton
class AvalancheController @Inject()(val configService: ConfigurationService, val logger: Logger, implicit val dal: CachedDAL, authorizations: Authorizations, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods with ExternalIdService {

  import authorizations._

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def newAvalancheId() = Action { implicit request => try {
      val newExtId = reserveNewExtId
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
        case Some(avalanche) if isAuthorizedToEdit(request.user, editKeyOpt, extId) => Ok(writeJson(avalancheReadWriteData(avalanche, images)))
        case Some(avalanche) => Ok(writeJson(avalancheReadOnlyData(avalanche, images)))
        case _ => NotFound
      }
    }
  }
}

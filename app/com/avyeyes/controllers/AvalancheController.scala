package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.JsonSerializers
import com.avyeyes.system.UserEnvironment
import org.json4s.Formats
import org.json4s.jackson.Serialization.write
import play.api.Logger
import securesocial.core.SecureSocial

import scala.concurrent.ExecutionContext

@Singleton
class AvalancheController @Inject()(dal: CachedDAL, jsonSerializers: JsonSerializers, authorizations: Authorizations, logger: Logger, implicit val env: UserEnvironment) extends SecureSocial {
  import authorizations._
  import jsonSerializers._

  implicit val formats: Formats = jsonSerializers.formats
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def find(extId: String) = UserAwareAction { implicit request =>
    logger.debug(s"finding avalanche $extId")
    dal.getAvalanche(extId) match {
      case Some(avalanche) => Ok(write(avalancheInitViewData(avalanche)))
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
        case Some(avalanche) if isAuthorizedToEdit(request.user, editKeyOpt, extId) => Ok(write(avalancheReadWriteData(avalanche, images)))
        case Some(avalanche) => Ok(write(avalancheReadOnlyData(avalanche, images)))
        case _ => NotFound
      }
    }
  }
}

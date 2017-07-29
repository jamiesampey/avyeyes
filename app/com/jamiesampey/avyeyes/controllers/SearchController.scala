package com.jamiesampey.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.jamiesampey.avyeyes.data.{AvalancheSpatialQuery, AvalancheTableQuery, CachedDao}
import com.jamiesampey.avyeyes.model.{AvyEyesUser, Coordinate}
import com.jamiesampey.avyeyes.service.AvyEyesUserService.AdminRoles
import com.jamiesampey.avyeyes.service.ConfigurationService
import com.jamiesampey.avyeyes.system.UserEnvironment
import com.jamiesampey.avyeyes.util.Constants.{AvyDistRangeMiles, CamPitchCutoff}
import org.json4s.JsonAST._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Action
import securesocial.core.SecureSocial

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class SearchController @Inject()(val configService: ConfigurationService, val logger: Logger,
                                 val dao: CachedDao, authorizations: Authorizations,
                                 val messagesApi: MessagesApi, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods with I18nSupport {

  import authorizations._

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def find(extId: String, editKeyOpt: Option[String]) = UserAwareAction.async { implicit request =>
    findAvalanche(extId, editKeyOpt, request.user)
  }

  private[controllers] def findAvalanche(extId: String, editKeyOpt: Option[String], user: Option[AvyEyesUser]) = {
    logger.debug(s"Requested avalanche $extId")

    dao.getAvalanche(extId).map { avalanche => dao.getAvalancheImages(extId).map { images =>
      if (isAdmin(user)) {
        logger.debug(s"Sending admin data for avalanche $extId")
        Ok(writeJson(avalancheAdminData(avalanche, images)))
      } else if (isAuthorizedToEdit(extId, user, editKeyOpt)) {
        logger.debug(s"Sending read-write data for avalanche $extId")
        Ok(writeJson(avalancheReadWriteData(avalanche, images)))
      } else if (isAuthorizedToView(extId, user)) {
        logger.debug(s"Sending read-only data for avalanche $extId")
        Ok(writeJson(avalancheReadOnlyData(avalanche, images)))
      } else NotFound
    }}.getOrElse(Future { NotFound })
  }

  def spatialSearch(query: AvalancheSpatialQuery, camAltParam: Option[Double], camPitchParam: Option[Double], camLngParam: Option[Double], camLatParam: Option[Double]) = Action { implicit request =>
    query.geoBounds match {
      case geoBounds if geoBounds.isEmpty => BadRequest(Messages("msg.horizonInView"))
      case _ => Try(dao.getAvalanches(query)) match {
        case Success(avalanches) =>
          val filteredAvalanches = (camPitchParam, camLngParam, camLatParam) match {
            case (Some(camPitch), Some(camLng), Some(camLat)) if camPitch > CamPitchCutoff =>
              val camLocation = Coordinate(camLng, camLat, 0)
              avalanches.filter(_.location.distanceTo(camLocation) < AvyDistRangeMiles)
            case _ => avalanches
          }
          Ok(writeJson(JArray(filteredAvalanches.map(a => avalancheSearchResultData(a, camAltParam)))))
        case Failure(ex) =>
          val errorMsg = "Failed to retrieve avalanches in view"
          logger.error(errorMsg, ex)
          InternalServerError(errorMsg)
      }
    }
  }

  def tabularSearch(query: AvalancheTableQuery) = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Try(dao.getAvalanchesAdmin(query)) match {
      case Success(result) => Ok(writeAdminTableJson(result))
      case Failure(ex) =>
        logger.error("Failed to retrieve avalanches for table", ex)
        InternalServerError
    }
  }
}

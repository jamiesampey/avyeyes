package com.jamiesampey.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.jamiesampey.avyeyes.data.{AvalancheSpatialQuery, AvalancheTableQuery, CachedDao}
import com.jamiesampey.avyeyes.model.{AvyEyesUser, Coordinate}
import com.jamiesampey.avyeyes.service.AvyEyesUserService.AdminRoles
import com.jamiesampey.avyeyes.service.ConfigurationService
import com.jamiesampey.avyeyes.system.UserEnvironment
import com.jamiesampey.avyeyes.util.Constants.CamRangePinThreshold
import org.json4s.JsonAST._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Action
import securesocial.core.SecureSocial

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class SearchController @Inject()(val configService: ConfigurationService, val log: Logger,
                                 val dao: CachedDao, authorizations: Authorizations,
                                 val messagesApi: MessagesApi, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods with I18nSupport {

  import authorizations._

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def find(extId: String, editKeyOpt: Option[String]) = UserAwareAction.async { implicit request =>
    findAvalanche(extId, editKeyOpt, request.user)
  }

  private[controllers] def findAvalanche(extId: String, editKeyOpt: Option[String], user: Option[AvyEyesUser]) = {
    log.debug(s"Requested avalanche $extId")

    dao.getAvalanche(extId).map { avalanche => dao.getAvalancheImages(extId).map { images =>
      if (isAdmin(user)) {
        log.debug(s"Sending admin data for avalanche $extId")
        Ok(writeJson(avalancheAdminData(avalanche, images)))
      } else if (isAuthorizedToEdit(extId, user, editKeyOpt)) {
        log.debug(s"Sending read-write data for avalanche $extId")
        Ok(writeJson(avalancheReadWriteData(avalanche, images)))
      } else if (isAuthorizedToView(extId, user)) {
        log.debug(s"Sending read-only data for avalanche $extId")
        Ok(writeJson(avalancheReadOnlyData(avalanche, images)))
      } else NotFound
    }}.getOrElse(Future { NotFound })
  }

  def spatialSearch(query: AvalancheSpatialQuery, camAltParam: Option[Double], camLngParam: Option[Double], camLatParam: Option[Double]) = Action { implicit request =>
    query.geoBounds match {
      case geoBounds if geoBounds.isEmpty => BadRequest(Messages("help.horizonInView"))
      case _ => Try(dao.getAvalanches(query)) match {
        case Success(avalanches) =>
          (camAltParam, camLngParam, camLatParam) match {
            case (Some(camAlt), Some(camLng), Some(camLat)) =>
              val camLocation = Coordinate(longitude = camLng, latitude = camLat, altitude = camAlt)
              val (nearAvalanches, farAvalanches) = avalanches.partition(_.location.ecefDistanceTo(camLocation) < CamRangePinThreshold)
              Ok(writeJson(JArray(nearAvalanches.map(a => avalanchePathSearchResult(a)) ++ farAvalanches.map(a => avalanchePinSearchResult(a)))))
            case _ =>
              Ok(writeJson(JArray(avalanches.map(a => avalanchePinSearchResult(a)))))
          }
        case Failure(ex) =>
          val errorMsg = "Failed to retrieve avalanches in view"
          log.error(errorMsg, ex)
          InternalServerError(errorMsg)
      }
    }
  }

  def tabularSearch(query: AvalancheTableQuery) = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Try(dao.getAvalanchesAdmin(query)) match {
      case Success(result) => Ok(writeAdminTableJson(result))
      case Failure(ex) =>
        log.error("Failed to retrieve avalanches for table", ex)
        InternalServerError
    }
  }
}

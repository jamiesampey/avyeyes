package com.avyeyes.controllers

import java.text.NumberFormat
import java.util.Locale
import javax.inject.{Inject, Singleton}

import com.avyeyes.data.{AvalancheSpatialQuery, AvalancheTableQuery, CachedDAL}
import com.avyeyes.model.Coordinate
import com.avyeyes.service.AvyEyesUserService.AdminRoles
import com.avyeyes.service.ConfigurationService
import com.avyeyes.system.UserEnvironment
import com.avyeyes.util.Constants.{AvyDistRangeMiles, CamAltitudeLimit, CamPitchCutoff}
import org.json4s.JsonAST._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Action
import securesocial.core.SecureSocial

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class SearchController @Inject()(val configService: ConfigurationService, val logger: Logger,
                                 val dal: CachedDAL, authorizations: Authorizations,
                                 val messagesApi: MessagesApi, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods with I18nSupport {

  import authorizations._

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  private val camAltLimitFormatted = NumberFormat.getNumberInstance(Locale.US).format(CamAltitudeLimit)

  def find(extId: String, editKeyOpt: Option[String]) = UserAwareAction.async { implicit request =>
    logger.debug(s"Requested avalanche $extId")

    dal.getAvalanche(extId).map { avalanche => dal.getAvalancheImages(extId).map { images =>
      if (isAdmin(request.user)) {
        logger.debug(s"Sending admin data for avalanche $extId")
        Ok(writeJson(avalancheAdminData(avalanche, images)))
      } else if (isAuthorizedToEdit(extId, request.user, editKeyOpt)) {
        logger.debug(s"Sending read-write data for avalanche $extId")
        Ok(writeJson(avalancheReadWriteData(avalanche, images)))
      } else if (isAuthorizedToView(extId, request.user)) {
        logger.debug(s"Sending read-only data for avalanche $extId")
        Ok(writeJson(avalancheReadOnlyData(avalanche, images)))
      } else NotFound
    }}.getOrElse(Future { NotFound })
  }

  def spatialSearch(query: AvalancheSpatialQuery, camAltParam: Option[Double], camPitchParam: Option[Double], camLngParam: Option[Double], camLatParam: Option[Double]) = Action { implicit request =>
    camAltParam match {
      case Some(camAlt) if camAlt > CamAltitudeLimit => BadRequest(Messages("msg.eyeTooHigh", camAltLimitFormatted))
      case _ if query.geoBounds.isEmpty => BadRequest(Messages("msg.horizonInView"))
      case _ => Try(dal.getAvalanches(query)) match {
        case Success(avalanches) if avalanches.isEmpty => BadRequest(Messages("msg.avySearchZeroMatches"))
        case Success(avalanches) =>
          val filteredAvalanches = (camPitchParam, camLngParam, camLatParam) match {
            case (Some(camPitch), Some(camLng), Some(camLat)) if camPitch > CamPitchCutoff =>
              val camLocation = Coordinate(camLng, camLat, 0)
              avalanches.filter(_.location.distanceTo(camLocation) < AvyDistRangeMiles)
            case _ => avalanches
          }
          Ok(writeJson(JArray(filteredAvalanches.map(avalancheSearchResultData))))
        case Failure(ex) =>
          val errorMsg = "Failed to retrieve avalanches in view"
          logger.error(errorMsg, ex)
          InternalServerError(errorMsg)
      }
    }
  }

  def tabularSearch(query: AvalancheTableQuery) = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Try(dal.getAvalanchesAdmin(query)) match {
      case Success(result) => Ok(writeAdminTableJson(result))
      case Failure(ex) =>
        logger.error("Failed to retrieve avalanches for table", ex)
        InternalServerError
    }
  }
}

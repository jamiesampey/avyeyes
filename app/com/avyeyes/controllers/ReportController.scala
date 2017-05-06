package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.Avalanche
import com.avyeyes.service.{ConfigurationService, ExternalIdService}
import org.json4s.JsonAST._
import play.api.Logger
import play.api.mvc.{Action, Controller}

import scala.util.{Try, Success, Failure}

@Singleton
class ReportController @Inject()(implicit val dal: CachedDAL, idService: ExternalIdService, val configService: ConfigurationService, val logger: Logger)
  extends Controller with Json4sMethods {

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

  def submitReport(extId: String) = Action(parse.tolerantText) { implicit request =>
    logger.info("received report submission")
    val avalancheOption: Option[Avalanche] = Try(readJson(Some(request.body)).extract[Avalanche]) match {
      case Success(avalancheFromData) =>
        logger.info(s"Successfully parsed avalanche $extId from report data")
        Some(avalancheFromData)
      case Failure(ex) =>
        logger.error(s"Unable to deserialize avalanche from report $extId", ex)
        None
    }

    avalancheOption.foreach(println(_))
    Ok
  }
}

package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.Avalanche
import com.avyeyes.service.{ConfigurationService, ExternalIdService}
import org.json4s
import org.json4s.Extraction
import org.json4s.JsonAST._
import play.api.Logger
import play.api.mvc.{Action, Controller}

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
    println(s"request body is: ${request.body}")
    val json = readJson(Some(request.body))

    val avalanche: Avalanche = Extraction.extract(json)
    println(s"avalanche is: $avalanche")

    Ok
  }
}

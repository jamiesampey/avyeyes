package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.service.{ConfigurationService, ExternalIdService}
import org.json4s.JsonAST._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
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

  val reportForm: Form[ReportData] = Form(
    mapping(
      "reportExtId" -> text,
      "areaName" -> text
    )(ReportData.apply)(ReportData.unapply)
  )

  def submitReport() = Action(parse.form(reportForm)) { implicit request =>
    logger.info("received report submission")
    val reportData: ReportData = request.body
    logger.info(s"extId is ${reportData.extId} and areaName is ${reportData.areaName}")

    Ok
  }
}

package com.jamiesampey.avyeyes.controllers

import com.jamiesampey.avyeyes.model.enums.ExperienceLevel
import com.jamiesampey.avyeyes.model.serializers.avyeyesFormats
import com.jamiesampey.avyeyes.model.{Avalanche, AvalancheImage}
import com.jamiesampey.avyeyes.service.ConfigurationService
import org.apache.commons.lang3.StringEscapeUtils.unescapeJava
import org.joda.time.format.DateTimeFormat
import org.json4s.Extraction
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, parse, render}
import play.api.mvc.RequestHeader

trait Json4sMethods {
  val configService: ConfigurationService

  implicit val formats = avyeyesFormats

  private[controllers] def avalancheAdminData(a: Avalanche, images: List[AvalancheImage]) = {
    ("viewable" -> a.viewable) ~ avalancheReadWriteData(a, images)
  }

  private[controllers] def avalancheReadWriteData(a: Avalanche, images: List[AvalancheImage]) = {
    ("submitterEmail" -> a.submitterEmail) ~ avalancheReadOnlyData(a, images)
  }

  private[controllers] def avalancheReadOnlyData(a: Avalanche, images: List[AvalancheImage]) = {
    ("extId" -> a.extId) ~
    ("location" -> Extraction.decompose(a.location)) ~
    ("title" -> a.title) ~
    ("areaName" -> a.areaName) ~
    ("date" -> Extraction.decompose(a.date)) ~
    ("submitterExp" -> ExperienceLevel.toCode(a.submitterExp)) ~
    ("weather" -> Extraction.decompose(a.weather)) ~
    ("slope" -> Extraction.decompose(a.slope)) ~
    ("classification" -> Extraction.decompose(a.classification)) ~
    ("comments" -> unescapeJava(a.comments.getOrElse(""))) ~
    ("images" -> Extraction.decompose(images)) ~
    ("coords" -> a.perimeter.flatMap(coord => Array(coord.longitude, coord.latitude, coord.altitude)))
  }

  private[controllers] def avalanchePathSearchResult(a: Avalanche) = {
    avalanchePinSearchResult(a) ~ ("coords" -> a.perimeter.flatMap(coord => Array(coord.longitude, coord.latitude, coord.altitude)))
  }

  private[controllers] def avalanchePinSearchResult(a: Avalanche) = {
    ("extId" -> a.extId) ~ ("title" -> a.title) ~ ("submitterExp" -> ExperienceLevel.toCode(a.submitterExp)) ~ ("slope" -> Extraction.decompose(a.slope)) ~ ("location" -> Extraction.decompose(a.location))
  }

  private[controllers] def writeJson(jValue: JValue): String = compact(render(jValue))

  private[controllers] def readJson(jsonOpt: Option[String]): JValue = jsonOpt match {
    case Some(jsonString) => parse(jsonString)
    case _ => JObject()
  }

  private val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private[controllers] def writeAdminTableJson(queryResult: (List[Avalanche], Int, Int))(implicit request: RequestHeader) = {
    writeJson(
      ("recordsTotal" -> queryResult._3) ~
        ("recordsFiltered" -> queryResult._2) ~
        ("records" -> queryResult._1.map(a =>
          ("created" -> a.createTime.toString(dtf)) ~
          ("updated" -> a.updateTime.toString(dtf)) ~
          ("extId" -> a.extId) ~
          ("viewable" -> a.viewable) ~
          ("areaName" -> a.areaName) ~
          ("submitter" -> a.submitterEmail) ~
          ("editKey" -> a.editKey)
      ))
    )
  }
}

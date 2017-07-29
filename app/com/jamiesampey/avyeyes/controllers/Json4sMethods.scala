package com.jamiesampey.avyeyes.controllers

import com.jamiesampey.avyeyes.model.enums.ExperienceLevel
import com.jamiesampey.avyeyes.model.serializers.avyeyesFormats
import com.jamiesampey.avyeyes.model.{Avalanche, AvalancheImage}
import com.jamiesampey.avyeyes.service.ConfigurationService
import com.jamiesampey.avyeyes.util.Constants.CamAltitudePinThreshold
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

  private[controllers] def avalancheReadOnlyData(a: Avalanche, images: List[AvalancheImage]) = {
    ("extId" -> a.extId) ~
    ("extUrl" -> configService.avalancheUrl(a.extId)) ~
    ("title" -> a.title) ~
    ("areaName" -> a.areaName) ~
    ("date" -> Extraction.decompose(a.date)) ~
    ("submitterExp" -> ExperienceLevel.toCode(a.submitterExp)) ~
    ("weather" -> Extraction.decompose(a.weather)) ~
    ("slope" -> Extraction.decompose(a.slope)) ~
    ("classification" -> Extraction.decompose(a.classification)) ~
    ("humanNumbers" -> Extraction.decompose(a.humanNumbers)) ~
    ("comments" -> unescapeJava(a.comments.getOrElse(""))) ~
    ("images" -> Extraction.decompose(images))
  }

  private[controllers] def avalancheReadWriteData(a: Avalanche, images: List[AvalancheImage]) = {
    ("submitterEmail" -> a.submitterEmail) ~ avalancheReadOnlyData(a, images)
  }

  private[controllers] def avalancheAdminData(a: Avalanche, images: List[AvalancheImage]) = {
    ("viewable" -> a.viewable) ~ avalancheReadWriteData(a, images)
  }

  private[controllers] def avalancheSearchResultData(a: Avalanche, camAltitude: Option[Double]) = {
    val locationField: JObject = if (camAltitude.map(_.toInt).getOrElse(CamAltitudePinThreshold + 1) < CamAltitudePinThreshold)
      "coords" -> a.perimeter.flatMap(coord => Array(coord.longitude, coord.latitude, coord.altitude))
    else "location" -> Extraction.decompose(a.location)

    ("extId" -> a.extId) ~ ("title" -> a.title) ~ ("submitterExp" -> ExperienceLevel.toCode(a.submitterExp)) ~ ("slope" -> Extraction.decompose(a.slope)) ~ locationField
  }

  private[controllers] def writeJson(jValue: JValue): String = compact(render(jValue))

  private[controllers] def readJson(jsonOpt: Option[String]): JValue = jsonOpt match {
    case Some(jsonString) => parse(jsonString)
    case _ => JObject()
  }

  private val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private[controllers] def writeAdminTableJson(queryResult: (List[Avalanche], Int, Int))(implicit request: RequestHeader) = {
    def getHttpsAvalancheLink(a: Avalanche) = {
      <a href={s"${configService.avalancheEditUrl(a)}"} target="adminViewWindow">{s"${a.title}"}</a>.toString
    }

    def getViewableElem(viewable: Boolean) = viewable match {
      case true => <span style="color: green;">Yes</span>.toString
      case false => <span style="color: red;">No</span>.toString
    }

    val drawVal = request.getQueryString("draw").map(_.toInt).getOrElse(throw new IllegalArgumentException)
    val matchingAvalanches = queryResult._1
    val filteredRecordCount = queryResult._2
    val totalRecordCount = queryResult._3

    writeJson(("draw" -> drawVal) ~ ("recordsTotal" -> totalRecordCount) ~ ("recordsFiltered" -> filteredRecordCount) ~
      ("data" -> matchingAvalanches.map(a => List(a.createTime.toString(dtf), a.updateTime.toString(dtf), a.extId, getViewableElem(a.viewable), getHttpsAvalancheLink(a), a.submitterEmail)))
    )
  }
}


//class ChainedEnumSerializer(enums: Enumeration*) extends Serializer[Enumeration#Value] {
//  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Enumeration#Value] = {
//    case code: (TypeInfo, JValue) => println(s"trying to deserialize $code"); AvalancheType.HS
//  }
//
//  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
//    case ev: Enumeration#Value => ev.toString.split('.').last
//  }
//}
package com.avyeyes.controllers

import com.avyeyes.model.enums._
import com.avyeyes.model.{Avalanche, AvalancheImage, Coordinate}
import com.avyeyes.service.ConfigurationService
import com.avyeyes.util.Converters.{dateToStr, strToDate}
import org.apache.commons.lang3.StringEscapeUtils.unescapeJava
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s.JsonAST.{JString, JValue}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Formats, JNull, Serializer, TypeInfo}
import play.api.mvc.RequestHeader

trait Json4sMethods {
  val configService: ConfigurationService

  implicit val formats: Formats = DefaultFormats +
    DateTimeSerializer +
    CoordinateSerializer +
    AvalancheImageSerializer +
    new ChainedEnumSerializer(Direction, AvalancheInterface, AvalancheTrigger, AvalancheTriggerModifier,
      AvalancheType, ExperienceLevel, ModeOfTravel, WindSpeed)

  private[controllers] def avalancheReadOnlyData(a: Avalanche, images: List[AvalancheImage]) = {
    ("extId" -> a.extId) ~
      ("extUrl" -> configService.avalancheUrl(a.extId)) ~
      ("title" -> a.title) ~
      ("areaName" -> a.areaName) ~
      ("date" -> Extraction.decompose(a.date)) ~
      ("submitterExp" -> Extraction.decompose(a.submitterExp)) ~
      ("weather" -> Extraction.decompose(a.weather)) ~
      ("slope" -> Extraction.decompose(a.slope)) ~
      ("classification" -> Extraction.decompose(a.classification)) ~
      ("humanNumbers" -> Extraction.decompose(a.humanNumbers)) ~
      ("comments" -> unescapeJava(a.comments.getOrElse(""))) ~
      ("images" -> Extraction.decompose(images))
  }

  private[controllers] def avalancheReadWriteData(a: Avalanche, images: List[AvalancheImage]) = {
    ("viewable" -> a.viewable) ~
      ("submitterEmail" -> a.submitterEmail) ~
      avalancheReadOnlyData(a, images)
  }

  private[controllers] def avalancheSearchResultData(a: Avalanche) = {
    ("extId" -> a.extId) ~
      ("title" -> a.title) ~
      ("coords" -> a.perimeter.flatMap(coord => Array(coord.longitude, coord.latitude, coord.altitude)))
  }

  private[controllers] def avalancheInitViewData(a: Avalanche) = {
    avalancheSearchResultData(a) ~
      ("submitterExp" -> Extraction.decompose(a.submitterExp)) ~
      ("slope" -> Extraction.decompose(a.slope))
  }

  private[controllers] def writeJson(jValue: JValue): String = compact(render(jValue))

  private val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private[controllers] def writeAdminTableJson(queryResult: (List[Avalanche], Int, Int))(implicit request: RequestHeader) = {
    def getHttpsAvalancheLink(a: Avalanche) = {
      <a href={s"${configService.avalancheUrl(a.extId)}"} target="adminViewWindow">{s"${a.title}"}</a>.toString
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

object DateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => strToDate(s)
    case JNull => null
  },
  {
    case d: DateTime => JString(dateToStr(d))
  }
))

object CoordinateSerializer extends CustomSerializer[Coordinate](format => (
  {
    case _: JValue => ???
  },
  {
    case c: Coordinate => ("latitude" -> c.latitude) ~ ("longitude" -> c.longitude) ~ ("altitude" -> c.altitude)
  }
))

object AvalancheImageSerializer extends CustomSerializer[AvalancheImage](format => (
  {
    case _: JValue => ???
  },
  {
    case AvalancheImage(createTime, avyExtId, filename, origFilename, mimeType, size, order, caption) =>
      ("filename" -> filename) ~ ("mimeType" -> mimeType) ~ ("size" -> size) ~ ("caption" -> caption)
  }
))


class ChainedEnumSerializer(enums: Enumeration*) extends Serializer[Enumeration#Value] {
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Enumeration#Value] = ???

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case ev: Enumeration#Value => ev.toString.split('.').last
  }
}
package com.avyeyes.controllers

import com.avyeyes.model.enums._
import com.avyeyes.model.{Avalanche, AvalancheImage, Coordinate}
import com.avyeyes.service.ConfigurationService
import com.avyeyes.util.Converters.{dateToStr, strToDate}
import org.apache.commons.lang3.StringEscapeUtils.unescapeJava
import org.joda.time.DateTime
import org.json4s.JsonDSL._
import org.json4s.JsonAST.{JString, JValue}
import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Formats, JNull, Serializer, TypeInfo}

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

  private[controllers] def writeJson(jValue: JValue) = compact(render(jValue))
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
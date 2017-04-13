package com.avyeyes.model

import javax.inject.Inject

import com.avyeyes.service.ConfigurationService
import com.avyeyes.util.Converters._
import org.apache.commons.lang3.StringEscapeUtils._
import org.joda.time.DateTime
import org.json4s.JsonAST.JString
import org.json4s.JsonDSL._
import org.json4s._
import play.api.i18n.Messages

class JsonSerializers @Inject()(urlHelper: ConfigurationService)(implicit val messages: Messages) {

  implicit val formats: Formats = DefaultFormats + DateTimeSerializer + CoordinateSerializer + AvalancheImageSerializer

  def avalancheReadOnlyData(a: Avalanche, images: List[AvalancheImage]) = {
    ("extId" -> a.extId) ~
    ("extUrl" -> urlHelper.avalancheUrl(a.extId)) ~
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

  def avalancheReadWriteData(a: Avalanche, images: List[AvalancheImage]) = {
    ("viewable" -> a.viewable) ~
    ("submitterEmail" -> a.submitterEmail) ~
    avalancheReadOnlyData(a, images)
  }

  def avalancheSearchResultData(a: Avalanche) = {
    ("extId" -> a.extId) ~
    ("title" -> a.title) ~
    ("coords" -> a.perimeter.flatMap(coord => Array(coord.longitude, coord.latitude, coord.altitude)))
  }

  def avalancheInitViewData(a: Avalanche) = {
    avalancheSearchResultData(a) ~
    ("submitterExp" -> Extraction.decompose(a.submitterExp)) ~
    ("slope" -> Extraction.decompose(a.slope))
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
    case json: JValue => ???
  },
  {
    case c: Coordinate => ("latitude" -> c.latitude) ~ ("longitude" -> c.longitude) ~ ("altitude" -> c.altitude)
  }
  ))

object AvalancheImageSerializer extends CustomSerializer[AvalancheImage](format => (
  {
    case json: JValue => ???
  },
  {
    case AvalancheImage(createTime, avyExtId, filename, origFilename, mimeType, size, order, caption) =>
      ("filename" -> filename) ~ ("mimeType" -> mimeType) ~ ("size" -> size) ~ ("caption" -> caption)
  }
))

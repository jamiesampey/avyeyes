package com.avyeyes.model

import javax.inject.Inject

import com.avyeyes.model.enums._
import com.avyeyes.service.ConfigurationService
import com.avyeyes.util.Converters._
import org.json4s.JsonAST.JString
import org.json4s.JsonDSL._
import org.json4s._
import org.apache.commons.lang3.StringEscapeUtils._
import org.joda.time.DateTime
import play.api.i18n.Messages


class JsonSerializers @Inject()(urlHelper: ConfigurationService) {

  implicit val formats: Formats = DefaultFormats +
    DateTimeSerializer +
    CoordinateSerializer +
    AvalancheImageSerializer +
    new ChainedEnumSerializer(Direction, AvalancheInterface, AvalancheTrigger, AvalancheTriggerModifier,
      AvalancheType, ExperienceLevel, ModeOfTravel, WindSpeed)


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

class ChainedEnumSerializer(enums: Enumeration*) extends Serializer[Enumeration#Value] {
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Enumeration#Value] = ???

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case ev: Enumeration#Value => {
      val tokens = ev.toString.split('.')
      tokens.length match {
        case 2 => ("label" -> getLocalizedLabel(tokens)) ~ ("value" -> tokens(1))
        case 3 =>
          ("category" -> Messages(s"enum.${tokens(0)}.${tokens(1)}")) ~
          ("label" -> getLocalizedLabel(tokens)) ~
          ("value" -> tokens(2))
      }

    }
  }

  private def getLocalizedLabel(tokens: Array[String]): String = {
    val label = if (tokens.last == "empty") "" else Messages(s"enum.${tokens.mkString(".")}")

    compositeLabelEnums.contains(tokens.head) match {
      case true => s"${tokens.last} - $label"
      case false => label
    }
  }

  private val compositeLabelEnums = {
    def getSimpleEnumName(enum: AutocompleteEnum) = enum.getClass.getSimpleName.replace("$", "")

    Seq(
      getSimpleEnumName(AvalancheInterface),
      getSimpleEnumName(AvalancheTrigger),
      getSimpleEnumName(AvalancheTriggerModifier),
      getSimpleEnumName(AvalancheType)
    )
  }
}
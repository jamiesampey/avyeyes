package com.avyeyes.model

import com.avyeyes.model.enums._
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.service.Injectors
import com.avyeyes.util.Converters._
import net.liftweb.http.S
import net.liftweb.json.JsonAST.JString
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.apache.commons.lang3.StringEscapeUtils._
import org.joda.time.DateTime


object JsonSerializers {
  val R = Injectors.resources.vend

  implicit val formats: Formats = DefaultFormats +
    DateTimeSerializer +
    AvalancheImageSerializer +
    new ChainedEnumSerializer(Direction, AvalancheInterface, AvalancheTrigger, AvalancheType,
      ExperienceLevel, ModeOfTravel, WindSpeed)


  def avalancheDetails(a: Avalanche, images: List[AvalancheImage]) = {
    ("extId" -> a.extId) ~
    ("extUrl" -> R.getAvalancheUrl(a.extId)) ~
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

  def avalancheAdminDetails(a: Avalanche, images: List[AvalancheImage]) = {
    ("viewable" -> a.viewable) ~
    ("submitterEmail" -> a.submitterEmail) ~
    avalancheDetails(a, images)
  }

  def avalancheSearchResult(a: Avalanche) = {
    ("extId" -> a.extId) ~
    ("coords" -> a.perimeter.flatMap(coord =>
      Array(coord.longitude, coord.latitude, coord.altitude)))
  }

  def avalancheInitView(a: Avalanche) = {
    avalancheSearchResult(a) ~
    ("date" -> Extraction.decompose(a.date)) ~
    ("areaName" -> a.areaName) ~
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
      tokens.size match {
        case 2 => ("label" -> getLocalizedLabel(tokens)) ~ ("value" -> tokens(1))
        case 3 =>
          ("category" -> S ? s"enum.${tokens(0)}.${tokens(1)}") ~
          ("label" -> getLocalizedLabel(tokens)) ~
          ("value" -> tokens(2))
      }

    }
  }

  private def getLocalizedLabel(tokens: Array[String]): String = {
    val label = S ? (if (tokens.last == "U") "enum.U" else s"enum.${tokens.mkString(".")}")

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
      getSimpleEnumName(AvalancheType)
    )
  }
}
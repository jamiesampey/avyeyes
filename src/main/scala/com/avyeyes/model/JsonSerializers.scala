package com.avyeyes.model

import com.avyeyes.model.enums._
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.util.Helpers._
import net.liftweb.http.S
import net.liftweb.json.JsonAST.JString
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime


object JsonSerializers {
  implicit val formats: Formats = DefaultFormats +
    DateTimeSerializer +
    AvalancheImageSerializer +
    new ChainedEnumSerializer(Aspect, AvalancheInterface, AvalancheTrigger, AvalancheType,
      ExperienceLevel, ModeOfTravel, Precipitation, SkyCoverage)
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
    case json: JValue =>
      AvalancheImage(
        filename = (json \ "filename").extract[String],
        mimeType = (json \ "mimeType").extract[String],
        size = (json \ "size").extract[Int]
      )
  },
  {
    case AvalancheImage(createTime, avyExtId, filename, origFilename, mimeType, size) =>
      ("filename" -> filename) ~ ("mimeType" -> mimeType) ~ ("size" -> size)
  }
))

class ChainedEnumSerializer(enums: Enumeration*) extends Serializer[Enumeration#Value] {
  private val predicate = classOf[Enumeration#Value]

  private def throwOn(value: JValue) =
    throw new MappingException("Can't convert %s to any of (%s)".format(value, enums.mkString(", ")))

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Enumeration#Value] = {
    case (TypeInfo(`predicate`, _), json) => json match {
      case wrapped @ JString(value) =>
        enums.flatMap { _.values.find(_.toString == value)}.headOption getOrElse throwOn(wrapped)
      case value => throwOn(value)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case ev: Enumeration#Value => {
      val tokens = ev.toString.split('.')
      ("value" -> tokens(1)) ~ ("label" -> getLocalizedLabel(tokens))
    }
  }

  private def getLocalizedLabel(tokens: Array[String]): String = {
    def useCompositLabel(enumClass: String) = enumClass match {
      case "AvalancheInterface" => true
      case "AvalancheTrigger" => true
      case "AvalancheType" => true
      case "Precipitation" => true
      case "SkyCoverage" => true
      case _ => false
    }

    val labelKey = if (tokens(1) == "U") "enum.U" else s"enum.${tokens(0)}.${tokens(1)}"
    val label = S ? labelKey

    useCompositLabel(tokens(0)) match {
      case true => s"${tokens(1)} - $label"
      case false => label
    }
  }
}
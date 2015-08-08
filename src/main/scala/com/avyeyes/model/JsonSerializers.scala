package com.avyeyes.model

import com.avyeyes.model.enums._
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.util.Helpers._
import net.liftweb.http.S
import net.liftweb.json.JsonAST.JString
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.apache.commons.lang3.StringEscapeUtils._
import org.joda.time.DateTime


object JsonSerializers {
  implicit val formats: Formats = DefaultFormats +
    DateTimeSerializer +
    AvalancheImageSerializer +
    new ChainedEnumSerializer(Aspect, AvalancheInterface, AvalancheTrigger, AvalancheType,
      ExperienceLevel, ModeOfTravel, Precipitation, SkyCoverage)


  def avalancheDetails(a: Avalanche, images: List[AvalancheImage]) = {
    ("extId" -> a.extId) ~
      ("extUrl" -> a.getExtHttpUrl) ~
      ("areaName" -> a.areaName) ~
      ("avyDate" -> Extraction.decompose(a.date)) ~
      ("submitterExp" -> Extraction.decompose(a.submitterExp)) ~
      ("scene" -> Extraction.decompose(a.scene)) ~
      ("slope" -> Extraction.decompose(a.slope)) ~
      ("classification" -> Extraction.decompose(a.classification)) ~
      ("humanNumbers" -> Extraction.decompose(a.humanNumbers)) ~
      ("comments" -> (if (a.comments.isDefined) unescapeJava(a.comments.get) else "")) ~
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

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Enumeration#Value] = ???

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case ev: Enumeration#Value => {
      val tokens = ev.toString.split('.')
      ("value" -> tokens(1)) ~ ("label" -> getLocalizedLabel(tokens))
    }
  }

  private def getLocalizedLabel(tokens: Array[String]): String = {
    val labelKey = if (tokens(1) == "U") "enum.U" else s"enum.${tokens(0)}.${tokens(1)}"
    val label = S ? labelKey

    compositeLabelEnums.contains(tokens(0)) match {
      case true => s"${tokens(1)} - $label"
      case false => label
    }
  }

  private val compositeLabelEnums = {
    def getSimpleEnumName(enum: AutocompleteEnum) = enum.getClass.getSimpleName.replace("$", "")

    Seq(
      getSimpleEnumName(AvalancheInterface),
      getSimpleEnumName(AvalancheTrigger),
      getSimpleEnumName(AvalancheType),
      getSimpleEnumName(Precipitation),
      getSimpleEnumName(SkyCoverage)
    )
  }
}
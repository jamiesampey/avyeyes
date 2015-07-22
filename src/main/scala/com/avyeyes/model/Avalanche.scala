package com.avyeyes.model

import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.util.Helpers._
import net.liftweb.json.Extraction
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import org.apache.commons.lang3.StringEscapeUtils._
import org.joda.time.DateTime
import JsonFormats.formats

case class Avalanche(
  createTime: DateTime,
  updateTime: DateTime,
  extId: String,
  viewable: Boolean,
  submitterEmail: String,
  submitterExp: ExperienceLevel,
  location: Coordinate,
  areaName: String,
  date: DateTime,
  scene: Scene,
  slope: Slope,
  classification: Classification,
  humanNumbers: HumanNumbers,
  perimeter: List[Coordinate],
  comments: Option[String]) {

  def getTitle() = s"${dateToStr(date)}: ${areaName}"

  def getExtHttpUrl() = s"${getHttpBaseUrl}${extId}"

  def getExtHttpsUrl() = s"${getHttpsBaseUrl}${extId}"

  def toJson = {
    ("extId" -> extId) ~
    ("extUrl" -> getExtHttpUrl) ~
    ("areaName" -> areaName) ~
    ("avyDate" -> dateToStr(date)) ~
    ("submitterExp" -> Extraction.decompose(submitterExp)) ~
    ("scene" -> Extraction.decompose(scene)) ~
    ("slope" -> Extraction.decompose(slope)) ~
    ("classification" -> Extraction.decompose(classification)) ~
    ("humanNumbers" -> Extraction.decompose(humanNumbers)) ~
    ("comments" -> getComments)
  }
  
  def toSearchResultJson = {
    JObject(List(
      JField("extId", JString(extId)),
      JField("aspect", JString(slope.aspect.toString)),
      JField("coords", JArray(perimeter.flatMap(coord =>
        Array(JDouble(coord.longitude), JDouble(coord.latitude), JDouble(coord.altitude)))))
    ))
  }

  private def getComments = comments match {
    case Some(str) => unescapeJava(str)
    case None => ""
  }
}





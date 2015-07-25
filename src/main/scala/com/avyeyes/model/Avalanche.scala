package com.avyeyes.model

import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.util.Helpers._
import net.liftweb.json.Extraction
import net.liftweb.json.JsonDSL._
import org.apache.commons.lang3.StringEscapeUtils._
import org.joda.time.DateTime

case class Avalanche(
  createTime: DateTime,
  updateTime: DateTime,
  extId: String,
  viewable: Boolean,
  submitterEmail: String,
  submitterExp: ExperienceLevel,
  location: Coordinate,
  date: DateTime,
  areaName: String,
  scene: Scene,
  slope: Slope,
  classification: Classification,
  humanNumbers: HumanNumbers,
  perimeter: List[Coordinate],
  comments: Option[String]) {

  def getTitle() = s"${dateToStr(date)}: ${areaName}"

  def getExtHttpUrl() = s"${getHttpBaseUrl}${extId}"

  def getExtHttpsUrl() = s"${getHttpsBaseUrl}${extId}"

  def toDetailsJson(images: List[AvalancheImage]) = {
    ("extId" -> extId) ~
    ("extUrl" -> getExtHttpUrl) ~
    ("areaName" -> areaName) ~
    ("avyDate" -> Extraction.decompose(date)) ~
    ("submitterExp" -> Extraction.decompose(submitterExp)) ~
    ("scene" -> Extraction.decompose(scene)) ~
    ("slope" -> Extraction.decompose(slope)) ~
    ("classification" -> Extraction.decompose(classification)) ~
    ("humanNumbers" -> Extraction.decompose(humanNumbers)) ~
    ("comments" -> (if (comments.isDefined) unescapeJava(comments.get) else "")) ~
    ("images" -> Extraction.decompose(images))
  }

  def toAdminDetailsJson(images: List[AvalancheImage]) = {
    ("viewable" -> viewable) ~
    ("submitterEmail" -> submitterEmail) ~
    toDetailsJson(images)
  }

  def toSearchJson = {
    ("extId" -> extId) ~
    ("aspect" -> slope.aspect.toString) ~
    ("coords" -> perimeter.flatMap(coord => Array(coord.longitude, coord.latitude, coord.altitude)))
  }
}

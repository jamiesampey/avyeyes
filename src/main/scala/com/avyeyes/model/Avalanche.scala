package com.avyeyes.model

import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.model.enums.{Aspect, Precipitation, SkyCoverage, _}
import com.avyeyes.util.Helpers._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime

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
    ("extId" -> extId) ~ ("extUrl" -> getExtHttpUrl) ~
      ("areaName" -> areaName) ~ ("avyDate" -> dateToStr(date)) ~
      ("submitterExp" -> ExperienceLevel.toJObject(submitterExp)) ~
      ("sky" -> SkyCoverage.toJObject(scene.skyCoverage)) ~ ("precip" -> Precipitation.toJObject(scene.precipitation)) ~
      ("elevation" -> location.altitude) ~ ("aspect" -> Aspect.toJObject(slope.aspect)) ~ 
      ("angle" -> slope.angle) ~ ("avyType" -> AvalancheType.toJObject(classification.avyType)) ~
      ("avyTrigger" -> AvalancheTrigger.toJObject(classification.trigger)) ~
      ("avyInterface" -> AvalancheInterface.toJObject(classification.interface)) ~
      ("rSize" -> classification.rSize) ~ ("dSize" -> classification.dSize) ~
      ("caught" -> humanNumbers.caught) ~ ("partiallyBuried" -> humanNumbers.partiallyBuried) ~
      ("fullyBuried" -> humanNumbers.fullyBuried) ~ ("injured" -> humanNumbers.injured) ~
      ("killed" -> humanNumbers.killed) ~ ("modeOfTravel" -> ModeOfTravel.toJObject(humanNumbers.modeOfTravel)) ~
      ("comments" -> comments)
  }
  
  def toSearchResultJson = {
    JObject(List(
      JField("extId", JString(extId)),
      JField("aspect", JString(slope.aspect.toString)),
      JField("coords", JArray(perimeter.flatMap(coord =>
        Array(JDouble(coord.longitude), JDouble(coord.latitude), JDouble(coord.altitude)))))
    ))
  }

}





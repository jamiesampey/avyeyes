package com.avyeyes.model

import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.model.enums._
import com.avyeyes.util.Helpers._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime

trait Avalanche {
  def createTime: DateTime
  def updateTime: DateTime
  def extId: String
  def viewable: Boolean
  def submitterEmail: String
  def submitterExp: ExperienceLevel
  def location: Coordinate
  def areaName: String
  def date: DateTime
  def scene: Scene
  def slope: Slope
  def classification: Classification
  def humanNumbers: HumanNumbers
  def comments: String
  def perimeter: List[Coordinate]

  def getSubmitter(): User = ???

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
  
  def toSearchResultJson = JObject(List(
    JField("extId", JString(extId)),
    JField("aspect", JString(slope.aspect.toString)),
    JField("coords", JArray(perimeter.map(coord => JString(coord.toString))))
  ))

}

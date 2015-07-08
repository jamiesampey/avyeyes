package com.avyeyes.model

import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect._
import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import com.avyeyes.model.enums.Precipitation
import com.avyeyes.model.enums.Precipitation._
import com.avyeyes.model.enums.SkyCoverage
import com.avyeyes.model.enums.SkyCoverage._
import com.avyeyes.model.enums._
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
  modeOfTravel: ModeOfTravel,
  comments: String,
  perimeter: List[Coordinate]) {

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
      ("killed" -> humanNumbers.killed) ~ ("modeOfTravel" -> ModeOfTravel.toJObject(modeOfTravel)) ~
      ("comments" -> comments)
  }
  
  def toSearchResultJson = JObject(List(
    JField("extId", JString(extId)),
    JField("aspect", JString(slope.aspect.toString)),
    JField("coords", JArray(perimeter.map(coord => JString(coord.toString))))
  ))

}

case class Scene(skyCoverage: SkyCoverage = SkyCoverage.U,
                 precipitation: Precipitation = Precipitation.U)

case class Slope(aspect: Aspect = Aspect.N,
                 angle: Int = 0,
                 elevation: Int = 0)

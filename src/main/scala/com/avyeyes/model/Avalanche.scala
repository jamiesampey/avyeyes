package com.avyeyes.model

import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect._
import com.avyeyes.model.enums.AvalancheInterface
import com.avyeyes.model.enums.AvalancheInterface._
import com.avyeyes.model.enums.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheTrigger._
import com.avyeyes.model.enums.AvalancheType
import com.avyeyes.model.enums.AvalancheType._
import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.model.enums.ModeOfTravel
import com.avyeyes.model.enums.ModeOfTravel._
import com.avyeyes.model.enums.Precipitation
import com.avyeyes.model.enums.Precipitation._
import com.avyeyes.model.enums.SkyCoverage
import com.avyeyes.model.enums.SkyCoverage._
import com.avyeyes.model.enums._
import com.avyeyes.util.Helpers._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._


case class Avalanche(id: Long,
                     createTime: DateTime,
                     updateTime: DateTime,
                     extId: String,
                     viewable: Boolean,
                     submitter: User,
                     submitterExp: ExperienceLevel,
                     location: Coordinate,
                     areaName: String,
                     date: DateTime,
                     scene: Scene,
                     slope: Slope,
                     classification: Classification,
                     humanNumbers: HumanNumbers,
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
      ("killed" -> humanNumbers.killed) ~ ("modeOfTravel" -> ModeOfTravel.toJObject(humanNumbers.modeOfTravel)) ~
      ("comments" -> comments)
  }
  
  def toSearchResultJson = JObject(List(
    JField("extId", JString(extId)),
    JField("aspect", JString(slope.aspect.toString)),
    JField("coords", JArray(perimeter.map(coord => JString(coord.toString))))
  ))

}

case class LiftedAvalanche(id: Rep[Long],
                           createTime: Rep[DateTime],
                           updateTime: Rep[DateTime],
                           extId: Rep[String],
                           viewable: Rep[Boolean],
                           submitter: LiftedUser,
                           submitterExp: Rep[ExperienceLevel],
                           location: LiftedCoordinate,
                           areaName: Rep[String],
                           date: Rep[DateTime],
                           scene: LiftedScene,
                           slope: LiftedSlope,
                           classification: LiftedClassification,
                           humanNumbers: LiftedHumanNumbers,
                           comments: Rep[String],
                           perimeter: List[LiftedCoordinate])

class Avalanches(tag: Tag) extends Table[Avalanche](tag, "avalanche") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def createTime = column[DateTime]("create_time")
  def updateTime = column[DateTime]("update_time")
  def extId = column[String]("external_id")
  def viewable = column[Boolean]("viewable")
  def submitter = column[Long]("submitter")
  def submitterExp = column[ExperienceLevel]("submitter_experience")
  def longitude = column[Double]("longitude")
  def latitude = column[Double]("latitude")
  def areaName = column[String]("area_name")
  def date = column[DateTime]("date")
  def sky = column[SkyCoverage]("sky")
  def precip = column[Precipitation]("precip")
  def elevation = column[Double]("elevation")
  def aspect = column[Aspect]("aspect")
  def angle = column[Int]("angle")
  def avyType = column[AvalancheType]("avalanche_type")
  def trigger = column[AvalancheTrigger]("avalanche_trigger")
  def interface = column[AvalancheInterface]("avalanche_interface")
  def rSize = column[Double]("r_size")
  def dSize = column[Double]("d_size")
  def caught = column[Int]("caught")
  def partiallyBuried = column[Int]("partially_buried")
  def fullyBuried = column[Int]("fully_buried")
  def injuried = column[Int]("injured")
  def killed = column[Int]("killed")
  def modeOfTravel = column[ModeOfTravel]("mode_of_travel")
  def comments = column[String]("comments")
  def perimeter = column[Seq[Coordinate]]("perimeter")

  def projection = ???
  def * = projection

}

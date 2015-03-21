package com.avyeyes.model

import com.avyeyes.model.enums._

import com.avyeyes.util.Helpers._
import com.avyeyes.persist.AvyEyesSchema
import org.joda.time.DateTime
import org.squeryl.dsl.ManyToOne

case class Avalanche(extId: String, viewable: Boolean, submitterExp: ExperienceLevel.Value, // metadata
  lat: Double, lng: Double, areaName: String, // location
  avyDate: DateTime, sky: Sky.Value, precip: Precip.Value, // temporal
  elevation: Int, aspect: Aspect.Value, angle: Int, // slope characteristics
  avyType: AvalancheType.Value, avyTrigger: AvalancheTrigger.Value, avyInterface: AvalancheInterface.Value,
  rSize: Double, dSize: Double, // avy characteristics
  caught: Int, partiallyBuried: Int, fullyBuried: Int, injured: Int, killed: Int, // human numbers
  modeOfTravel: ModeOfTravel.Value, comments: String, kmlCoords: String, var submitterId: Long = -1)
  extends UpdatableSquerylDbObj {
  
  def this() = this("", false, ExperienceLevel.A0,
      0.0, 0.0, "", DateTime.now,
      Sky.U, Precip.U, 0, Aspect.N, 0, 
      AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")

  lazy private val submitterRelation: ManyToOne[User] = AvyEyesSchema.userToAvalanches.right(this)

  def getSubmitter(): User = submitterRelation.single

  def getTitle() = s"${dateToStr(avyDate)}: ${areaName}"

  def getExtHttpUrl() = s"${getHttpBaseUrl}${extId}"

  def getExtHttpsUrl() = s"${getHttpsBaseUrl}${extId}"
}
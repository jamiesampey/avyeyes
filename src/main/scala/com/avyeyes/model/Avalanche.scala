package com.avyeyes.model

import com.avyeyes.model.enums._
import java.util.Date

import com.avyeyes.util.Helpers._
import com.avyeyes.persist.AvyEyesSchema
import org.squeryl.dsl.ManyToOne

case class Avalanche(extId: String, viewable: Boolean, submitterExp: ExperienceLevel.Value, // metadata
  lat: Double, lng: Double, areaName: String, // location
  avyDate: Date, sky: Sky.Value, precip: Precip.Value, // temporal
  elevation: Int, aspect: Aspect.Value, angle: Int, // slope characteristics
  avyType: AvalancheType.Value, avyTrigger: AvalancheTrigger.Value, avyInterface: AvalancheInterface.Value,
  rSize: Double, dSize: Double, // avy characteristics
  caught: Int, partiallyBuried: Int, fullyBuried: Int, injured: Int, killed: Int, // human numbers
  modeOfTravel: ModeOfTravel.Value, comments: String, kmlCoords: String, var submitterId: Long = -1)
  extends UpdatableSquerylDbObj {
  
  def this() = this("", false, ExperienceLevel.A0,
      0.0, 0.0, "", new Date(), 
      Sky.U, Precip.U, 0, Aspect.N, 0, 
      AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")

  lazy val submitter: ManyToOne[User] = AvyEyesSchema.userToAvalanches.right(this)

  def getTitle() = s"${dateToStr(avyDate)}: ${areaName}"

  def getExtUrl() = s"${getHttpBaseUrl}${extId}"
}
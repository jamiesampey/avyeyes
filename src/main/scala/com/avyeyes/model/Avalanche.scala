package com.avyeyes.model

import com.avyeyes.model.enums._
import java.util.Date

class Avalanche(val extId: String, val viewable: Boolean, /* metadata */
    val submitterEmail: String, val submitterExp: ExperienceLevel.Value, val submitterYearsExp: Int, 
    val lat: Double, val lng: Double, val areaName: String, /* location */
    val avyDate: Date, val sky: Sky.Value, val precip: Precip.Value, /* temporal */
    val elevation: Int, val aspect: Aspect.Value, val angle: Int, /* slope characteristics */
    val avyType: AvalancheType.Value, val trigger: AvalancheTrigger.Value, val bedSurface: AvalancheInterface.Value, 
    val rSize: Double, val dSize: Double, /* avy characteristics */
    val caught: Int, val partiallyBuried: Int, val fullyBuried: Int, val injured: Int, val killed: Int, /* human numbers */
    val modeOfTravel: ModeOfTravel.Value, val comments: Option[String], 
    val kmlCoords: String) extends AvalancheObj {
  
  def this() = this("", false, "", ExperienceLevel.A0, 0, 
      0.0, 0.0, "", new Date(), 
      Sky.U, Precip.U, 0, Aspect.N, 0, 
      AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, None, "")
}
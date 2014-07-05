package com.avyeyes.model

import com.avyeyes.model.enums._
import java.util.Date

class Avalanche(val extId: String, val viewable: Boolean, /* metadata */
    val lat: Double, val lng: Double, val areaName: String, /* location */
    val avyDate: Date, val sky: Sky.Value, val precip: Precip.Value, /* temporal */
    val elevation: Int, val aspect: Aspect.Value, val angle: Int, /* slope characteristics */
    val avyType: AvalancheType.Value, val trigger: AvalancheTrigger.Value, val bedSurface: AvalancheInterface.Value, 
    val rSize: Double, val dSize: Double, /* avy characteristics */
    val caught: Int, val partiallyBuried: Int, val fullyBuried: Int, val injured: Int, val killed: Int, /* human numbers */
    val modeOfTravel: ModeOfTravel.Value,
    val comments: Option[String], val submitterEmail: Option[String], 
    val kmlCoords: String) extends AvyDbObj {
  
  def this() = this("", false, 0.0, 0.0, "", new Date(), 
      Sky.UNKNOWN, Precip.UNKNOWN, 0, Aspect.UNKNOWN, 0, 
      AvalancheType.UNKNOWN, AvalancheTrigger.UNKNOWN, AvalancheInterface.UNKNOWN, 0.0, 0.0, 
      -1, -1, -1, -1, -1, ModeOfTravel.UNKNOWN, 
      None, None, "")
}
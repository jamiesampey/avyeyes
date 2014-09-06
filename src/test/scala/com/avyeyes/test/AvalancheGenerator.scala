package com.avyeyes.test

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.util.AEHelpers._

trait AvalancheGenerator {

  def avalancheAtLocation(extId: String, viewable: Boolean, lat: Double, lng: Double): Avalanche = {
    Avalanche(extId, viewable, "tester@company.com", ExperienceLevel.A0, lat, lng, "test title", parseDateStr("07-24-2014"), 
      Sky.U, Precip.U, 12171, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, None, "")
  }
    
}
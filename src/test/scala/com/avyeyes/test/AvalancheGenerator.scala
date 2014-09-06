package com.avyeyes.test

import com.avyeyes.model._
import com.avyeyes.model.enums._
import java.util.Date

trait AvalancheGenerator {

  def avalancheAtLocation(extId: String, viewable: Boolean, lat: Double, lng: Double): Avalanche = {
    Avalanche(extId, viewable, "tester@company.com", ExperienceLevel.A0, lat, lng, "test title", new Date, 
      Sky.U, Precip.U, 12171, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")
  }
  
  def avalancheAtLocationWithAspect(extId: String, viewable: Boolean, lat: Double, lng: Double, aspect: Aspect.Value) = {
    Avalanche(extId, viewable, "tester@company.com", ExperienceLevel.A0, lat, lng, "test title", new Date, 
      Sky.U, Precip.U, 12171, aspect, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")
  }
  
  def avalancheAtLocationWithCoords(extId: String, viewable: Boolean, lat: Double, lng: Double, coords: String): Avalanche = {
    Avalanche(extId, viewable, "tester@company.com", ExperienceLevel.A0, lat, lng, "test title", new Date, 
      Sky.U, Precip.U, 12171, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", coords)
  }
}
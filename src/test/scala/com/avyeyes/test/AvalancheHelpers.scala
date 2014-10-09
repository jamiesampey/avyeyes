package com.avyeyes.test

import com.avyeyes.model._
import com.avyeyes.model.enums._
import java.util.Date

import com.avyeyes.persist.AvalancheDao

trait AvalancheHelpers {

  def insertTestAvalanche(dao: AvalancheDao, a: Avalanche) = dao.insertAvalanche(a, "thomas.jefferson@gmail.com")

  def avalancheAtLocation(extId: String, viewable: Boolean, lat: Double, lng: Double): Avalanche = {
    Avalanche(extId, viewable, ExperienceLevel.A0, lat, lng, "test title", new Date,
      Sky.U, Precip.U, 2849, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")
  }
  
  def avalancheAtLocationWithAspect(extId: String, viewable: Boolean, lat: Double, lng: Double, aspect: Aspect.Value) = {
    Avalanche(extId, viewable, ExperienceLevel.A0, lat, lng, "test title", new Date,
      Sky.U, Precip.U, 2849, aspect, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")
  }
  
  def avalancheAtLocationWithCoords(extId: String, viewable: Boolean, lat: Double, lng: Double, coords: String): Avalanche = {
    Avalanche(extId, viewable, ExperienceLevel.A0, lat, lng, "test title", new Date,
      Sky.U, Precip.U, 2849, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", coords)
  }
  
  def avalancheAtLocationOnDate(extId: String, viewable: Boolean, lat: Double, lng: Double, date: Date): Avalanche = {
    Avalanche(extId, viewable, ExperienceLevel.A0, lat, lng, "test title", date,
      Sky.U, Precip.U, 2849, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")
  }
  
  def avalancheAtLocationWithTypeAndTrigger(extId: String, viewable: Boolean, lat: Double, lng: Double, 
    avyType: AvalancheType.Value, avyTrigger: AvalancheTrigger.Value): Avalanche = {
    Avalanche(extId, viewable, ExperienceLevel.A0, lat, lng, "test title", new Date,
      Sky.U, Precip.U, 2849, Aspect.N, 45, avyType, avyTrigger, AvalancheInterface.U, 
      0.0, 0.0, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")
  }
  
  def avalancheAtLocationWithSize(extId: String, viewable: Boolean, lat: Double, lng: Double, 
    rSize: Double, dSize: Double): Avalanche = {
    Avalanche(extId, viewable, ExperienceLevel.A0, lat, lng, "test title", new Date,
      Sky.U, Precip.U, 2849, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      rSize, dSize, -1, -1, -1, -1, -1, ModeOfTravel.U, "", "")
  }
  
  def avalancheAtLocationWithCaughtKilledNumbers(extId: String, viewable: Boolean, lat: Double, lng: Double, 
    caught: Int, killed: Int): Avalanche = {
    Avalanche(extId, viewable, ExperienceLevel.A0, lat, lng, "test title", new Date,
      Sky.U, Precip.U, 2849, Aspect.N, 45, AvalancheType.U, AvalancheTrigger.U, AvalancheInterface.U, 
      0.0, 0.0, caught, -1, -1, -1, killed, ModeOfTravel.U, "", "")
  }
}
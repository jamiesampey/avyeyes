package com.avyeyes.test

import java.util.Date

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvalancheDao

trait AvalancheHelpers {

  def insertTestAvalanche(dao: AvalancheDao, a: Avalanche) = dao.insertAvalanche(a, "thomas.jefferson@gmail.com")
  def insertTestAvalanche(dao: AvalancheDao, a: Avalanche, submitterEmail: String) = dao.insertAvalanche(a, submitterEmail)

  def avalancheAtLocation(extId: String, viewable: Boolean, lat: Double, lng: Double): Avalanche = {
    getTestAvalanche(extId, viewable, lat, lng)
  }
  
  def avalancheWithAspect(extId: String, viewable: Boolean, lat: Double, lng: Double, aspect: Aspect.Value) = {
    getTestAvalanche(extId, viewable, lat, lng, aspect = aspect)
  }
  
  def avalancheWithCoords(extId: String, viewable: Boolean, lat: Double, lng: Double, coords: String): Avalanche = {
    getTestAvalanche(extId, viewable, lat, lng, coords = coords)
  }
  
  def avalancheOnDate(extId: String, viewable: Boolean, lat: Double, lng: Double, date: Date): Avalanche = {
    getTestAvalanche(extId, viewable, lat, lng, avyDate = date)
  }

  def avalancheWithTypeAndTrigger(extId: String, viewable: Boolean, lat: Double, lng: Double,
    avyType: AvalancheType.Value, avyTrigger: AvalancheTrigger.Value): Avalanche = {
    getTestAvalanche(extId, viewable, lat, lng, avyType = avyType, avyTrigger = avyTrigger)
  }
  
  def avalancheWithSize(extId: String, viewable: Boolean, lat: Double, lng: Double, 
    rSize: Double, dSize: Double): Avalanche = {
    getTestAvalanche(extId, viewable, lat, lng, rSize = rSize, dSize = dSize)
  }
  
  def avalancheWithCaughtKilledNumbers(extId: String, viewable: Boolean, lat: Double, lng: Double, 
    caught: Int, killed: Int): Avalanche = {
    getTestAvalanche(extId, viewable, lat, lng, caught = caught, killed = killed)
  }

  def avalancheWithNameAndSubmitter(extId: String, viewable: Boolean, lat: Double, lng: Double,
    areaName: String, email: String): Avalanche = {
    getTestAvalanche(extId, viewable, lat, lng, areaName = areaName, submitterEmail = email)
  }
  
  private def getTestAvalanche(testExtId: String, viewable: Boolean, lat: Double, lng: Double, 
    submitterExp: ExperienceLevel.Value = ExperienceLevel.A0, areaName: String = "", avyDate: Date = new Date, 
    sky: Sky.Value = Sky.U, precip: Precip.Value = Precip.U, elevation: Int = 2849, aspect: Aspect.Value = Aspect.N, 
    angle: Int = 45, avyType: AvalancheType.Value = AvalancheType.U, avyTrigger: AvalancheTrigger.Value = AvalancheTrigger.U, 
    avyInterface: AvalancheInterface.Value = AvalancheInterface.U, rSize: Double = 0.0, dSize: Double = 0.0,
    caught: Int = 0, partiallyBuried: Int = 0, fullyBuried: Int = 0, injured: Int = 0, killed: Int = 0,
    modeOfTravel: ModeOfTravel.Value = ModeOfTravel.U, comments: String = "", coords: String = "", 
    submitterEmail: String = ""): Avalanche = {
  
    new Avalanche(testExtId, viewable, submitterExp, lat, lng, areaName, avyDate, sky, precip, 
      elevation, aspect, angle, avyType, avyTrigger, avyInterface, rSize, dSize,
      caught, partiallyBuried, fullyBuried, injured, killed, modeOfTravel, comments, coords) {
      override def getSubmitter() = new User(submitterEmail)}
  }
}
package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.test._
import com.avyeyes.util.AEHelpers._

import net.liftweb.http._

class AvyDetailsTest extends WebSpec2 with MockPersistence with AvalancheGenerator with LiftHelpers {
  // Testing an OBJECT (singleton), so the mockAvalancheDao is inserted ONCE. 
  // Only one chance to mock all methods.

  val extId1 = "4jf93dkj"
  val a1 = avalancheAtLocation(extId1, true, 41.6634870900582, -103.875046142935)
  mockAvalancheDao.selectViewableAvalanche(extId1) returns Some(a1)  
  mockAvalancheDao.selectAvalancheImageFilenames(extId1) returns Nil
      
  val badExtId = "59fke4k0"
  val noAvalanche: Option[Avalanche] = None
  mockAvalancheDao.selectViewableAvalanche(badExtId) returns noAvalanche
      
  "Valid avalanche details REST request" should {
    "Return avalanche details" withSFor(s"http://avyeyes.com/rest/avydetails/$extId1") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(AvyDetails(req)())
        
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") must_== extId1
      extractJsonStringField(resp, "extUrl") must endWith(extId1)
    }
    
    "Return reader-friendly labels for enum code fields" withSFor(s"http://avyeyes.com/rest/avydetails/$extId1") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(AvyDetails(req)())
      
      extractJsonStringField(resp, "submitterExp") must_== ExperienceLevel.getEnumLabel(a1.submitterExp)
      extractJsonStringField(resp, "sky") must_== Sky.getEnumLabel(a1.sky)
      extractJsonStringField(resp, "precip") must_== Precip.getEnumLabel(a1.precip)
      extractJsonStringField(resp, "aspect") must_== Aspect.getEnumLabel(a1.aspect)
      extractJsonStringField(resp, "avyType") must_== AvalancheType.getEnumLabel(a1.avyType)
      extractJsonStringField(resp, "trigger") must_== AvalancheTrigger.getEnumLabel(a1.trigger)
      extractJsonStringField(resp, "bedSurface") must_== AvalancheInterface.getEnumLabel(a1.bedSurface)      
      extractJsonStringField(resp, "modeOfTravel") must_== ModeOfTravel.getEnumLabel(a1.modeOfTravel)
    }
  }
  
  "Invalid avalanche details REST request" should {
    "Return NotFoundResponse (404)" withSFor(s"http://avyeyes.com/rest/avydetails/$badExtId") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(AvyDetails(req)())
    
      resp must beAnInstanceOf[NotFoundResponse]
    }
  }
}

package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.test._
import com.avyeyes.util.Helpers._

import net.liftweb.http._

class AvyDetailsTest extends WebSpec2 with MockPersistence with AvalancheHelpers with LiftHelpers {
  // Testing an OBJECT (singleton), so the mockAvalancheDao is inserted ONCE. 
  // Only one chance to mock all methods.

  val extId1 = "4jf93dkj"
  val a1 = avalancheAtLocation(extId1, true, 41.6634870900582, -103.875046142935)
  mockAvalancheDao.selectAvalanche(extId1) returns Some(a1)  
  mockAvalancheDao.selectAvalancheImagesMetadata(extId1) returns Nil
      
  val badExtId = "59fke4k0"
  val noAvalanche: Option[Avalanche] = None
  mockAvalancheDao.selectAvalanche(badExtId) returns noAvalanche
      
  "Valid avalanche details REST request" should {
    "Return avalanche details" withSFor(s"http://avyeyes.com/rest/avydetails/$extId1") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(AvyDetails(req)())
        
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") must_== extId1
      extractJsonStringField(resp, "extUrl") must endWith(extId1)
    }
    
    "Return JSON objects for enum (autocomplete) fields" withSFor(s"http://avyeyes.com/rest/avydetails/$extId1") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(AvyDetails(req)())
      
      extractJsonField(resp, "submitterExp") must_== ExperienceLevel.toJObject(a1.submitterExp)
      extractJsonField(resp, "sky") must_== Sky.toJObject(a1.sky)
      extractJsonField(resp, "precip") must_== Precip.toJObject(a1.precip)
      extractJsonField(resp, "aspect") must_== Aspect.toJObject(a1.aspect)
      extractJsonField(resp, "avyType") must_== AvalancheType.toJObject(a1.avyType)
      extractJsonField(resp, "avyTrigger") must_== AvalancheTrigger.toJObject(a1.avyTrigger)
      extractJsonField(resp, "avyInterface") must_== AvalancheInterface.toJObject(a1.avyInterface)      
      extractJsonField(resp, "modeOfTravel") must_== ModeOfTravel.toJObject(a1.modeOfTravel)
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

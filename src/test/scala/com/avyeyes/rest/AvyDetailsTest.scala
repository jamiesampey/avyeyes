package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.test._
import net.liftweb.http._

class AvyDetailsTest extends WebSpec2 with MockInjectors with Generators with LiftHelpers {
  val avyDetails = new AvyDetails

  "Valid avalanche details REST request" should {
    val extId1 = "4jf93dkj"
    val a1 = avalancheAtLocation(extId1, true, 41.6634870900582, -103.875046142935)
    mockAvalancheDao.getAvalanche(extId1) returns Some(a1)
    mockAvalancheDao.getAvalancheImages(extId1) returns Nil

    "Return avalanche details" withSFor(s"http://avyeyes.com/rest/avydetails/$extId1") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())
        
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") must_== extId1
      extractJsonStringField(resp, "extUrl") must endWith(extId1)
    }
    
    "Return JSON objects for enum (autocomplete) fields" withSFor(s"http://avyeyes.com/rest/avydetails/$extId1") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())
      
      extractJsonField(resp, "submitterExp") must_== ExperienceLevel.toJObject(a1.submitterExp)
      extractJsonField(resp, "sky") must_== SkyCoverage.toJObject(a1.sky)
      extractJsonField(resp, "precip") must_== Precipitation.toJObject(a1.precip)
      extractJsonField(resp, "aspect") must_== Aspect.toJObject(a1.aspect)
      extractJsonField(resp, "avyType") must_== AvalancheType.toJObject(a1.avyType)
      extractJsonField(resp, "avyTrigger") must_== AvalancheTrigger.toJObject(a1.avyTrigger)
      extractJsonField(resp, "avyInterface") must_== AvalancheInterface.toJObject(a1.avyInterface)      
      extractJsonField(resp, "modeOfTravel") must_== ModeOfTravel.toJObject(a1.modeOfTravel)
    }
  }
  
  "Invalid avalanche details REST request" should {
    val badExtId = "59fke4k0"
    val noAvalanche: Option[Avalanche] = None
    mockAvalancheDao.getAvalanche(badExtId) returns noAvalanche

    "Return NotFoundResponse (404)" withSFor(s"http://avyeyes.com/rest/avydetails/$badExtId") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())
    
      resp must beAnInstanceOf[NotFoundResponse]
    }
  }
}

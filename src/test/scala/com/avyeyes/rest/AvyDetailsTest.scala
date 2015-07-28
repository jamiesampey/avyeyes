package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import net.liftweb.http._
import net.liftweb.json.Extraction

class AvyDetailsTest extends WebSpec2 with MockInjectors with LiftHelpers {
  val avyDetails = new AvyDetails

  "Valid avalanche details REST request" should {
    val extId1 = "4jf93dkj"
    val a1 = avalancheForTest.copy(extId = extId1, viewable = true)
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

      extractJsonField(resp, "submitterExp") must_== Extraction.decompose(a1.submitterExp)
      extractJsonField(resp, "scene") must_== Extraction.decompose(a1.scene)
      extractJsonField(resp, "slope") must_== Extraction.decompose(a1.slope)
      extractJsonField(resp, "classification") must_== Extraction.decompose(a1.classification)
      extractJsonField(resp, "humanNumbers") must_== Extraction.decompose(a1.humanNumbers)
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

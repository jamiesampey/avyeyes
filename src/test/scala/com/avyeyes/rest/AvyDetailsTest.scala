package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.test.Generators._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.test._
import net.liftweb.http._
import net.liftweb.json.Extraction

class AvyDetailsTest extends WebSpec2 with MockInjectors {
  val avyDetails = new AvyDetails

  "Valid avalanche details REST request" should {
    val a1 = avalancheForTest.copy(viewable = true)
    mockAvalancheDal.getAvalancheFromDisk(a1.extId) returns Some(a1)
    mockAvalancheDal.getAvalancheImages(a1.extId) returns Nil

    "Return avalanche details" withSFor s"http://avyeyes.com/rest/avydetails/${a1.extId}" in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual a1.extId
      extractJsonStringField(resp, "extUrl") must endWith(a1.extId)
    }

    "Return JSON objects for enum (autocomplete) fields" withSFor s"http://avyeyes.com/rest/avydetails/${a1.extId}" in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      import com.avyeyes.model.JsonSerializers.formats

      extractJsonField(resp, "submitterExp") mustEqual Extraction.decompose(a1.submitterExp)
      extractJsonField(resp, "scene") mustEqual Extraction.decompose(a1.scene)
      extractJsonField(resp, "slope") mustEqual Extraction.decompose(a1.slope)
      extractJsonField(resp, "classification") mustEqual Extraction.decompose(a1.classification)
      extractJsonField(resp, "humanNumbers") mustEqual Extraction.decompose(a1.humanNumbers)
    }
  }
  
  "Invalid avalanche details REST request" should {
    val badExtId = "59fke4k0"
    val noAvalanche: Option[Avalanche] = None
    mockAvalancheDal.getAvalancheFromDisk(badExtId) returns noAvalanche

    "Return NotFoundResponse (404)" withSFor s"http://avyeyes.com/rest/avydetails/$badExtId" in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())
    
      resp must beAnInstanceOf[NotFoundResponse]
    }
  }
}

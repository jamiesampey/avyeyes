package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.Avalanche
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.service.{ResourceService, Injectors}
import com.avyeyes.test.Generators._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.test._
import net.liftweb.http._
import net.liftweb.json.Extraction
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

class AvyDetailsTest extends WebSpec2 with AroundExample with Mockito {

  val mockResources = mock[ResourceService]
  val mockAvalancheDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.dal.doWith(mockAvalancheDal) {
        AsResult(t)
      }
    }

  "Valid avalanche details REST request" >> {
    isolated
    val a1 = avalancheForTest.copy(viewable = true)

    "Return avalanche details" withSFor s"http://avyeyes.com/rest/avydetails/${a1.extId}" in {
      mockAvalancheDal.getAvalancheFromDisk(a1.extId) returns Some(a1)
      mockAvalancheDal.getAvalancheImages(a1.extId) returns Nil

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual a1.extId
      extractJsonStringField(resp, "areaName") mustEqual a1.areaName
      extractJsonStringField(resp, "comments") mustEqual a1.comments.getOrElse("")
    }

    "Return JSON objects for enum (autocomplete) fields" withSFor s"http://avyeyes.com/rest/avydetails/${a1.extId}" in {
      mockAvalancheDal.getAvalancheFromDisk(a1.extId) returns Some(a1)
      mockAvalancheDal.getAvalancheImages(a1.extId) returns Nil

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      import com.avyeyes.model.JsonSerializers.formats

      extractJsonField(resp, "submitterExp") mustEqual Extraction.decompose(a1.submitterExp)
      extractJsonField(resp, "weather") mustEqual Extraction.decompose(a1.weather)
      extractJsonField(resp, "slope") mustEqual Extraction.decompose(a1.slope)
      extractJsonField(resp, "classification") mustEqual Extraction.decompose(a1.classification)
      extractJsonField(resp, "humanNumbers") mustEqual Extraction.decompose(a1.humanNumbers)
    }
  }
  
  "Invalid avalanche details REST request" >> {
    val avyDetails = new AvyDetails

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

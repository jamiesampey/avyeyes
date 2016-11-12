package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.Avalanche
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.service.{UserSession, ResourceService, Injectors}
import com.avyeyes.test.Generators._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.test._
import com.avyeyes.util.Constants._
import net.liftweb.http._
import net.liftweb.json.Extraction
import org.joda.time.DateTime
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

class AvyDetailsTest extends WebSpec2 with AroundExample with Mockito {

  val mockResources = mock[ResourceService]
  val mockUserSession = mock[UserSession]
  val mockAvalancheDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.user.doWith(mockUserSession) {
        Injectors.dal.doWith(mockAvalancheDal) {
          AsResult(t)
        }
      }
    }

  val oldAvalanche = avalancheForTest.copy(viewable = true, createTime = DateTime.now.minus(AvalancheEditWindow.toMillis + 30000))
  val newAvalanche = avalancheForTest.copy(viewable = true, createTime = DateTime.now.minus(AvalancheEditWindow.toMillis - 30000))

  "Valid avalanche details REST request" >> {
    isolated

    "Return read-only avalanche details" withSFor s"http://avyeyes.com/rest/avydetails/${oldAvalanche.extId}" in {
      mockUserSession.isAdminSession returns false
      mockAvalancheDal.getAvalancheFromDisk(oldAvalanche.extId) returns Some(oldAvalanche)
      mockAvalancheDal.getAvalancheImages(oldAvalanche.extId) returns Nil

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual oldAvalanche.extId
      extractJsonStringField(resp, "areaName") mustEqual oldAvalanche.areaName
      extractJsonStringField(resp, "comments") mustEqual oldAvalanche.comments.getOrElse("")
      extractJsonBoolOptionField(resp, "viewable") must beNone
      extractJsonStringOptionField(resp, "submitterEmail") must beNone
    }

    "Return edit details for admin session" withSFor s"http://avyeyes.com/rest/avydetails/${oldAvalanche.extId}" in {
      mockUserSession.isAdminSession returns true
      mockAvalancheDal.getAvalancheFromDisk(oldAvalanche.extId) returns Some(oldAvalanche)
      mockAvalancheDal.getAvalancheImages(oldAvalanche.extId) returns Nil

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual oldAvalanche.extId
      extractJsonStringField(resp, "areaName") mustEqual oldAvalanche.areaName
      extractJsonBoolOptionField(resp, "viewable").get mustEqual oldAvalanche.viewable
      extractJsonStringOptionField(resp, "submitterEmail").get mustEqual oldAvalanche.submitterEmail
    }

    "Return edit details within edit window with valid edit key" withSFor s"http://avyeyes.com/rest/avydetails/${newAvalanche.extId}?edit=${newAvalanche.editKey}" in {
      mockUserSession.isAdminSession returns false

      mockAvalancheDal.getAvalancheFromDisk(newAvalanche.extId) returns Some(newAvalanche)
      mockAvalancheDal.getAvalancheImages(newAvalanche.extId) returns Nil

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual newAvalanche.extId
      extractJsonStringField(resp, "areaName") mustEqual newAvalanche.areaName
      extractJsonBoolOptionField(resp, "viewable").get mustEqual newAvalanche.viewable
      extractJsonStringOptionField(resp, "submitterEmail").get mustEqual newAvalanche.submitterEmail
    }

    "Return JSON objects for enum (autocomplete) fields" withSFor s"http://avyeyes.com/rest/avydetails/${oldAvalanche.extId}" in {
      mockUserSession.isAdminSession returns false
      mockAvalancheDal.getAvalancheFromDisk(oldAvalanche.extId) returns Some(oldAvalanche)
      mockAvalancheDal.getAvalancheImages(oldAvalanche.extId) returns Nil

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      import com.avyeyes.model.JsonSerializers.formats

      extractJsonField(resp, "submitterExp") mustEqual Extraction.decompose(oldAvalanche.submitterExp)
      extractJsonField(resp, "weather") mustEqual Extraction.decompose(oldAvalanche.weather)
      extractJsonField(resp, "slope") mustEqual Extraction.decompose(oldAvalanche.slope)
      extractJsonField(resp, "classification") mustEqual Extraction.decompose(oldAvalanche.classification)
      extractJsonField(resp, "humanNumbers") mustEqual Extraction.decompose(oldAvalanche.humanNumbers)
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

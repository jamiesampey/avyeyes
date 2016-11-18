package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.service.{Injectors, ResourceService, UserSession}
import com.avyeyes.test.Generators._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.test._
import com.avyeyes.util.Constants._
import net.liftweb.common.Box
import net.liftweb.http._
import net.liftweb.json.Extraction
import org.joda.time.DateTime
import org.mockito.Matchers
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

import scala.concurrent.Future

class AvyDetailsTest extends WebSpec2 with AroundExample with Mockito {
  val mockResources = mock[ResourceService]
  val mockUser = mock[UserSession]
  val mockAvalancheDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.user.doWith(mockUser) {
        Injectors.dal.doWith(mockAvalancheDal) {
          AsResult(t)
        }
      }
    }

  val testAvalanche = avalancheForTest.copy(viewable = true, createTime = DateTime.now.minus(AvalancheEditWindow.toMillis + 30000))

  "Valid avalanche details REST request" >> {
    isolated

    mockAvalancheDal.getAvalancheFromDisk(testAvalanche.extId) returns Future.successful(Some(testAvalanche))
    mockAvalancheDal.getAvalancheImages(testAvalanche.extId) returns Future.successful(Nil)

    "Return read-write details for authorized user" withSFor s"http://avyeyes.com/rest/avydetails/${testAvalanche.extId}" in {
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(testAvalanche), any[Box[String]]) returns true

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual testAvalanche.extId
      extractJsonStringField(resp, "areaName") mustEqual testAvalanche.areaName
      extractJsonBoolOptionField(resp, "viewable").get mustEqual testAvalanche.viewable
      extractJsonStringOptionField(resp, "submitterEmail").get mustEqual testAvalanche.submitterEmail
    }

    "Return read-only avalanche details for unauthorized user" withSFor s"http://avyeyes.com/rest/avydetails/${testAvalanche.extId}" in {
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(testAvalanche), any[Box[String]]) returns false
      mockUser.isAuthorizedToViewAvalanche(Matchers.eq(testAvalanche)) returns true

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual testAvalanche.extId
      extractJsonStringField(resp, "areaName") mustEqual testAvalanche.areaName
      extractJsonStringField(resp, "comments") mustEqual testAvalanche.comments.getOrElse("")
      extractJsonBoolOptionField(resp, "viewable") must beNone
      extractJsonStringOptionField(resp, "submitterEmail") must beNone
    }

    "Return NotFoundResponse (404) for an unviewable avalanche" withSFor s"http://avyeyes.com/rest/avydetails/${testAvalanche.extId}" in {
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(testAvalanche), any[Box[String]]) returns false
      mockUser.isAuthorizedToViewAvalanche(Matchers.eq(testAvalanche)) returns false

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[NotFoundResponse]
    }

    "Return JSON objects for enum (autocomplete) fields" withSFor s"http://avyeyes.com/rest/avydetails/${testAvalanche.extId}" in {
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(testAvalanche), any[Box[String]]) returns false
      mockUser.isAuthorizedToViewAvalanche(Matchers.eq(testAvalanche)) returns true

      val avyDetails = new AvyDetails

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      import com.avyeyes.model.JsonSerializers.formats

      extractJsonField(resp, "submitterExp") mustEqual Extraction.decompose(testAvalanche.submitterExp)
      extractJsonField(resp, "weather") mustEqual Extraction.decompose(testAvalanche.weather)
      extractJsonField(resp, "slope") mustEqual Extraction.decompose(testAvalanche.slope)
      extractJsonField(resp, "classification") mustEqual Extraction.decompose(testAvalanche.classification)
      extractJsonField(resp, "humanNumbers") mustEqual Extraction.decompose(testAvalanche.humanNumbers)
    }
  }
  
  "Invalid avalanche details REST request" >> {
    val avyDetails = new AvyDetails

    val badExtId = "59fke4k0"
    mockAvalancheDal.getAvalancheFromDisk(badExtId) returns Future.successful(None)
    mockAvalancheDal.getAvalancheImages(badExtId) returns Future.successful(Nil)

    "Return NotFoundResponse (404)" withSFor s"http://avyeyes.com/rest/avydetails/$badExtId" in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())

      resp must beAnInstanceOf[NotFoundResponse]
    }
  }
}

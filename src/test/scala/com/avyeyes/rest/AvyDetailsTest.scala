package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.JsonSerializers.formats
import com.avyeyes.service.{UserSession, Injectors}
import com.avyeyes.test.Generators._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.test._
import com.avyeyes.util.Constants._
import com.avyeyes.util.FutureOps._
import net.liftweb.common.{Full, Box}
import net.liftweb.http._
import net.liftweb.json.Extraction
import org.joda.time.DateTime
import org.mockito.Matchers
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

import scala.concurrent.Future

class AvyDetailsTest extends WebSpec2 with AroundExample with Mockito {

  val mockUser = mock[UserSession]
  val mockAvalancheDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result =
    Injectors.user.doWith(mockUser) {
      Injectors.dal.doWith(mockAvalancheDal) {
        AsResult(t)
      }
    }

  val oldAvalanche = avalancheForTest.copy(viewable = true, createTime = DateTime.now.minus(AvalancheEditWindow.toMillis + 30000))
  val newAvalanche = avalancheForTest.copy(viewable = true, createTime = DateTime.now.minus(AvalancheEditWindow.toMillis - 30000))

  "Valid avalanche details REST request" >> {
    isolated

    "Return read-only avalanche details" withSFor s"http://avyeyes.com/rest/avydetails/${oldAvalanche.extId}" in {
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(oldAvalanche), any[Box[String]]) returns false
      mockUser.isAuthorizedToViewAvalanche(Matchers.eq(oldAvalanche)) returns true

      mockAvalancheDal.getAvalancheFromDisk(oldAvalanche.extId) returns Future.successful(Some(oldAvalanche))
      mockAvalancheDal.getAvalancheImages(oldAvalanche.extId) returns Future.successful(Nil)

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

    "Return read-write details for authorized user" withSFor s"http://avyeyes.com/rest/avydetails/${oldAvalanche.extId}" in {
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(oldAvalanche), any[Box[String]]) returns true
      mockAvalancheDal.getAvalancheFromDisk(oldAvalanche.extId) returns Future.successful(Some(oldAvalanche))
      mockAvalancheDal.getAvalancheImages(oldAvalanche.extId) returns Future.successful(Nil)

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
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(newAvalanche), Matchers.eq(Full(newAvalanche.editKey.toString))) returns true

      mockAvalancheDal.getAvalancheFromDisk(newAvalanche.extId).resolve returns Some(newAvalanche)
      mockAvalancheDal.getAvalancheImages(newAvalanche.extId).resolve returns Nil

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
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(oldAvalanche), any[Box[String]]) returns false
      mockUser.isAuthorizedToViewAvalanche(Matchers.eq(oldAvalanche)) returns true

      mockAvalancheDal.getAvalancheFromDisk(oldAvalanche.extId).resolve returns Some(oldAvalanche)
      mockAvalancheDal.getAvalancheImages(oldAvalanche.extId).resolve returns Nil

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
    mockAvalancheDal.getAvalancheFromDisk(badExtId).resolve returns None

    "Return NotFoundResponse (404)" withSFor s"http://avyeyes.com/rest/avydetails/$badExtId" in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(avyDetails(req)())
    
      resp must beAnInstanceOf[NotFoundResponse]
    }
  }
}

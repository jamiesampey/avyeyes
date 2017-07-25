package com.jamiesampey.avyeyes.controllers

import com.jamiesampey.avyeyes.data.{AvalancheSpatialQuery, CachedDao, GeoBounds}
import com.jamiesampey.avyeyes.service.AvyEyesUserService.AdminRole
import com.jamiesampey.avyeyes.service.ConfigurationService
import com.jamiesampey.avyeyes.util.Constants.CamAltitudeLimit
import helpers.BaseSpec
import org.joda.time.DateTime
import org.mockito.Mockito
import org.specs2.specification.BeforeEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsBoolean, JsString}
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.Future


class SearchControllerTest extends BaseSpec with BeforeEach with Json4sMethods {
  override val configService = mock[ConfigurationService]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  private val testExtId = "49fk349d"

  private val mockDao = mock[CachedDao]

  def before = {
    Mockito.reset(mockDao)
  }

  val appBuilder = new GuiceApplicationBuilder()
    .overrides(bind[CachedDao].toInstance(mockDao))

  val injector = appBuilder.injector()
  val subject = injector.instanceOf[SearchController]

  "Avalanche lookup" should {
    "retrieve admin data for an existing avalanche" in {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = false)
      val adminUser = genAvyEyesUser.generate.copy(roles = List(AdminRole))

      mockDao.getAvalanche(testExtId) returns Some(existingAvalanche)
      mockDao.getAvalancheImages(testExtId) returns Future { List.empty }

      val result = subject.findAvalanche(testExtId, None, Some(adminUser))
      val jsonResponse = contentAsJson(result)

      there was one(mockDao).getAvalanche(testExtId)
      there was one(mockDao).getAvalancheImages(testExtId)
      result.resolve.header.status mustEqual OK
      (jsonResponse \ "extId").as[JsString].value mustEqual testExtId
      (jsonResponse \ "viewable").as[JsBoolean].value must beFalse
      (jsonResponse \ "submitterEmail").as[JsString].value mustEqual existingAvalanche.submitterEmail
    }

    "retrieve read/write data for an existing avalanche with a valid edit key" in {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))

      mockDao.getAvalanche(testExtId) returns Some(existingAvalanche)
      mockDao.getAvalancheImages(testExtId) returns Future { List.empty }

      val result = subject.findAvalanche(testExtId, Some(existingAvalanche.editKey.toString), None)
      val jsonResponse = contentAsJson(result)

      result.resolve.header.status mustEqual OK
      (jsonResponse \ "extId").as[JsString].value mustEqual testExtId
      (jsonResponse \ "viewable").asOpt[JsBoolean] must beNone
      (jsonResponse \ "submitterEmail").as[JsString].value mustEqual existingAvalanche.submitterEmail
      (jsonResponse \ "areaName").as[JsString].value mustEqual existingAvalanche.areaName
    }

    "retrieve read-only data for an existing avalanche" in {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))

      mockDao.getAvalanche(testExtId) returns Some(existingAvalanche)
      mockDao.getAvalancheImages(testExtId) returns Future { List.empty }

      val result = subject.findAvalanche(testExtId, None, None)
      val jsonResponse = contentAsJson(result)

      result.resolve.header.status mustEqual OK
      (jsonResponse \ "extId").as[JsString].value mustEqual testExtId
      (jsonResponse \ "viewable").asOpt[JsBoolean] must beNone
      (jsonResponse \ "submitterEmail").asOpt[JsString] must beNone
      (jsonResponse \ "areaName").as[JsString].value mustEqual existingAvalanche.areaName
    }
  }

  "Avalanche spatial search" should {
    "not allow search if the camera altitude is too high" in new WithApplication(appBuilder.build) {
      val spatialQuery = AvalancheSpatialQuery(geoBounds = Some(GeoBounds(-104, -105, 39, 38)))

      val action = subject.spatialSearch(spatialQuery, Some(CamAltitudeLimit+1), None, None, None)
      val result = call(action, FakeRequest()).resolve

      result.header.status mustEqual BAD_REQUEST
    }

    "not allow search if the geographic bounds are not set (horizon is in view)" in new WithApplication(appBuilder.build) {
      val action = subject.spatialSearch(AvalancheSpatialQuery(), Some(CamAltitudeLimit-1000), None, None, None)
      val result = call(action, FakeRequest()).resolve

      result.header.status mustEqual BAD_REQUEST
    }

    "return a 404 if no avalanches were found" in new WithApplication(appBuilder.build) {
      mockDao.getAvalanches(any) returns List.empty
      val spatialQuery = AvalancheSpatialQuery(geoBounds = Some(GeoBounds(-104, -105, 39, 38)))

      val action = subject.spatialSearch(spatialQuery, Some(CamAltitudeLimit-1000), None, None, None)
      val result = call(action, FakeRequest()).resolve

      result.header.status mustEqual NOT_FOUND
    }

    "return avalanches matching avalanches" in new WithApplication(appBuilder.build) {
      val avalancheOne = genAvalanche.generate
      val avalancheTwo = genAvalanche.generate
      val avalanches = List(avalancheOne, avalancheTwo)

      mockDao.getAvalanches(any) returns avalanches
      val spatialQuery = AvalancheSpatialQuery(geoBounds = Some(GeoBounds(-104, -105, 39, 38)))

      val action = subject.spatialSearch(spatialQuery, Some(CamAltitudeLimit-1000), None, None, None)
      val result = call(action, FakeRequest())
      val jsonResponseArray = contentAsJson(result).as[JsArray].value

      result.resolve.header.status mustEqual OK
      jsonResponseArray must haveLength(2)
      (jsonResponseArray.head \ "extId").as[JsString].value mustEqual avalancheOne.extId
      (jsonResponseArray.last \ "extId").as[JsString].value mustEqual avalancheTwo.extId
    }
  }
}

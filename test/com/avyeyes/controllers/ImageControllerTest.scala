package com.avyeyes.controllers

import java.io.File

import com.avyeyes.data.CachedDao
import com.avyeyes.service.AvyEyesUserService._
import com.avyeyes.service.{AmazonS3Service, ExternalIdService}
import com.avyeyes.util.Constants.{MaxImagesPerAvalanche, ScreenshotFilename}
import helpers.BaseSpec
import org.joda.time.DateTime
import org.mockito.{Matchers, Mockito}
import org.specs2.specification.BeforeEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.test.{FakeRequest, WithApplication}
import securesocial.core.SecureSocial.RequestWithUser

import scala.concurrent.Future


class ImageControllerTest extends BaseSpec with BeforeEach {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  private val testExtId = "49fk349d"
  private val testImageFile = new File("public/images/avyeyes.jpg")

  private val mockDao = mock[CachedDao]
  private val mockS3Service = mock[AmazonS3Service]
  private val mockExtIdService = mock[ExternalIdService]

  def before = {
    Mockito.reset(mockDao, mockS3Service, mockExtIdService)
  }

  val appBuilder = new GuiceApplicationBuilder()
    .overrides(bind[ExternalIdService].toInstance(mockExtIdService))
    .overrides(bind[CachedDao].toInstance(mockDao))
    .overrides(bind[AmazonS3Service].toInstance(mockS3Service))

  val injector = appBuilder.injector()
  val subject = injector.instanceOf[ImageController]


  "Image upload" should {
    "not allow an image upload if the user is not authorized" in {
      val result = subject.doImagesUpload(testExtId, None, None, Seq.empty).resolve

      there was no(mockS3Service).uploadImage(any, any, any, any)
      there was no(mockDao).insertAvalancheImage(any)
      result.header.status mustEqual UNAUTHORIZED
    }

    "allow an image upload if the avalanche if the avalanche report is in progress" in {
      mockExtIdService.reservationExists(testExtId) returns true // reservation exists
      mockDao.countAvalancheImages(testExtId) returns Future(0)
      mockS3Service.uploadImage(Matchers.eq(testExtId), any, any, any) returns Future.successful { }
      mockDao.insertAvalancheImage(any) returns Future.successful(1)
      mockDao.getAvalanche(Matchers.eq(testExtId)) returns Some(genAvalanche.generate.copy(viewable = true))

      val result = subject.doImagesUpload(testExtId, None, None, Seq(testImageFile)).resolve

      there was one(mockS3Service).uploadImage(Matchers.eq(testExtId), any, any, any)
      there was one(mockDao).insertAvalancheImage(any)
      there was one(mockS3Service).allowPublicImageAccess(testExtId)
      result.header.status mustEqual OK
    }

    "allow an image upload if a valid editKey is present" in {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))

      mockExtIdService.reservationExists(testExtId) returns false
      mockDao.countAvalancheImages(testExtId) returns Future(0)
      mockS3Service.uploadImage(Matchers.eq(testExtId), any, any, any) returns Future.successful { }
      mockDao.insertAvalancheImage(any) returns Future.successful(1)
      mockDao.getAvalanche(Matchers.eq(testExtId)) returns Some(existingAvalanche)

      val result = subject.doImagesUpload(testExtId, Some(existingAvalanche.editKey.toString), None, Seq(testImageFile)).resolve

      there was one(mockS3Service).uploadImage(Matchers.eq(testExtId), any, any, any)
      there was one(mockDao).insertAvalancheImage(any)
      there was one(mockS3Service).allowPublicImageAccess(testExtId)
      result.header.status mustEqual OK
    }

    "not upload an image if the report already contains the max allowed number of images" in {
      val adminUser = genAvyEyesUser.generate.copy(roles = List(AdminRole))
      mockDao.countAvalancheImages(testExtId) returns Future(MaxImagesPerAvalanche)

      val result = subject.doImagesUpload(testExtId, None, Some(adminUser), Seq.empty).resolve

      there was no(mockS3Service).uploadImage(any, any, any, any)
      there was no(mockDao).insertAvalancheImage(any)
      result.header.status mustEqual BAD_REQUEST
    }
  }

  "Screenshot upload" should {
    "not allow a screenshot upload if the report is not in progress" in {
      mockExtIdService.reservationExists(testExtId) returns false

      val result = subject.doScreenshotUpload(testExtId, None, testImageFile).resolve

      there was no(mockS3Service).uploadImage(any, any, any, any)
      result.header.status mustEqual UNAUTHORIZED
    }

    "allow a screenshot upload for a new report" in {
      mockExtIdService.reservationExists(testExtId) returns true
      mockS3Service.uploadImage(Matchers.eq(testExtId), any, any, any) returns Future.successful {}

      val result = subject.doScreenshotUpload(testExtId, None, testImageFile).resolve

      there was one(mockS3Service).uploadImage(Matchers.eq(testExtId), Matchers.eq(ScreenshotFilename), Matchers.eq(subject.JpegMimeType), any)
      result.header.status mustEqual OK
    }
  }

  "Image order" should {
    "not allow image ordering if the user is not authorized to edit" in new WithApplication(appBuilder.build) {
      mockExtIdService.reservationExists(testExtId) returns false

      val imageOrderRequest = FakeRequest().withJsonBody(Json.obj("order" -> JsArray()))
      val requestWithUser = RequestWithUser(None, None, imageOrderRequest)

      val action = subject.order(testExtId, None)
      val result = call(action, requestWithUser).resolve

      there was no(mockDao).updateAvalancheImageOrder(any, any)
      result.header.status mustEqual UNAUTHORIZED
    }

    "order images within the report edit window" in new WithApplication(appBuilder.build) {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))
      mockExtIdService.reservationExists(testExtId) returns false
      mockDao.getAvalanche(Matchers.eq(testExtId)) returns Some(existingAvalanche)

      val imageOrderJsArray = JsArray(Seq(JsString("a129554e-859f-45ca-9ffb-b88d5b3e3bfa"), JsString("50cfcabc-c4b6-45d1-a5a2-862b5c5d8675"), JsString("39e4fab8-064f-49c1-8b4e-5f728277c0a8")))
      val imageOrderRequest = FakeRequest().withJsonBody(Json.obj("order" -> imageOrderJsArray))
      val requestWithUser = RequestWithUser(None, None, imageOrderRequest)

      val action = subject.order(testExtId, Some(existingAvalanche.editKey.toString))
      val result = call(action, requestWithUser).resolve

      there was one(mockDao).updateAvalancheImageOrder(testExtId, imageOrderJsArray.value.map(_.toString).toList)
      result.header.status mustEqual OK
    }
  }

  "Image caption" should {
    "not allow an image caption if the user is not authorized to edit" in new WithApplication(appBuilder.build) {
      mockExtIdService.reservationExists(testExtId) returns false

      val imageCaptionRequest = FakeRequest().withJsonBody(Json.obj("caption" -> JsString("")))
      val requestWithUser = RequestWithUser(None, None, imageCaptionRequest)

      val action = subject.caption(testExtId, "a129554e-859f-45ca-9ffb-b88d5b3e3bfa", None)
      val result = call(action, requestWithUser).resolve

      there was no(mockDao).updateAvalancheImageCaption(any, any, any)
      result.header.status mustEqual UNAUTHORIZED
    }

    "set an image caption within the report edit window" in new WithApplication(appBuilder.build) {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))
      mockExtIdService.reservationExists(testExtId) returns false
      mockDao.getAvalanche(Matchers.eq(testExtId)) returns Some(existingAvalanche)

      val baseFilename = "a129554e-859f-45ca-9ffb-b88d5b3e3bfa"

      val imageCaption = "here's a picture of an avalanche"
      val imageCaptionRequest = FakeRequest().withJsonBody(Json.obj("caption" -> JsString(imageCaption)))
      val requestWithUser = RequestWithUser(None, None, imageCaptionRequest)

      val action = subject.caption(testExtId, baseFilename, Some(existingAvalanche.editKey.toString))
      val result = call(action, requestWithUser).resolve

      there was one(mockDao).updateAvalancheImageCaption(testExtId, baseFilename, Some(imageCaption))
      result.header.status mustEqual OK
    }

    "clear an image caption within the report edit window" in new WithApplication(appBuilder.build) {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))
      mockExtIdService.reservationExists(testExtId) returns false
      mockDao.getAvalanche(Matchers.eq(testExtId)) returns Some(existingAvalanche)

      val baseFilename = "a129554e-859f-45ca-9ffb-b88d5b3e3bfa"

      val imageCaptionRequest = FakeRequest().withJsonBody(Json.obj("caption" -> JsString("")))
      val requestWithUser = RequestWithUser(None, None, imageCaptionRequest)

      val action = subject.caption(testExtId, baseFilename, Some(existingAvalanche.editKey.toString))
      val result = call(action, requestWithUser).resolve

      there was one(mockDao).updateAvalancheImageCaption(testExtId, baseFilename, None)
      result.header.status mustEqual OK
    }
  }

  "Image delete" should {
    "not allow an image delete if the user is not authorized to edit" in new WithApplication(appBuilder.build) {
      val baseFilename = "a129554e-859f-45ca-9ffb-b88d5b3e3bfa"
      val existingImage = genAvalancheImage.generate.copy(avalanche = testExtId, filename = s"$baseFilename.jpg")

      mockExtIdService.reservationExists(testExtId) returns false

      val requestWithUser = RequestWithUser(None, None, FakeRequest())

      val action = subject.delete(testExtId, baseFilename, None)
      val result = call(action, requestWithUser).resolve

      there was no(mockDao).getAvalancheImage(any, any)
      result.header.status mustEqual UNAUTHORIZED
    }

    "set an image caption within the report edit window" in new WithApplication(appBuilder.build) {
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))
      val baseFilename = "a129554e-859f-45ca-9ffb-b88d5b3e3bfa"
      val existingImage = genAvalancheImage.generate.copy(avalanche = testExtId, filename = s"$baseFilename.jpg")

      mockDao.getAvalanche(Matchers.eq(testExtId)) returns Some(existingAvalanche)
      mockDao.getAvalancheImage(testExtId, baseFilename) returns Future(Some(existingImage))
      mockDao.deleteAvalancheImage(testExtId, existingImage.filename) returns Future.successful {}
      mockExtIdService.reservationExists(testExtId) returns false

      val requestWithUser = RequestWithUser(None, None, FakeRequest())

      val action = subject.delete(testExtId, baseFilename, Some(existingAvalanche.editKey.toString))
      val result = call(action, requestWithUser).resolve

      there was one(mockDao).getAvalancheImage(testExtId, baseFilename)
      there was one(mockDao).deleteAvalancheImage(testExtId, existingImage.filename)
      there was one(mockS3Service).deleteImage(testExtId, existingImage.filename)
      result.header.status mustEqual OK
    }
  }
}

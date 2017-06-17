package com.avyeyes.controllers

import java.io.File

import com.avyeyes.data.CachedDAL
import com.avyeyes.service.AmazonS3Service
import com.avyeyes.service.AvyEyesUserService._
import com.avyeyes.util.Constants.MaxImagesPerAvalanche
import helpers.BaseSpec
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future


class ImageControllerTest extends BaseSpec {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  private val mockDAL = mock[CachedDAL]

  private val mockS3Service = mock[AmazonS3Service]

  val appBuilder = new GuiceApplicationBuilder()
    .overrides(bind[CachedDAL].toInstance(mockDAL))
    .overrides(bind[AmazonS3Service].toInstance(mockS3Service))

  val injector = appBuilder.injector()
  val subject = injector.instanceOf[ImageController]

  val imageFile = new File("public/images/avyeyes.jpg")

  val extId = "49fk349d"

  "ImageController" should {

    "not upload an image if the user is not authorized" in {
      mockDAL.countAvalancheImages(extId) returns Future(MaxImagesPerAvalanche - 3)

      val result = subject.uploadImages("49fk349d", None, None, Seq.empty).resolve

      there was no(mockS3Service).uploadImage(any, any, any, any)
      there was no(mockDAL).insertAvalancheImage(any)
      result.header.status mustEqual 401
    }

    "not upload an image if the report already contains the max allowed number of images" in {
      val adminUser = genAvyEyesUser.generate.copy(roles = List(AdminRole))
      mockDAL.countAvalancheImages(extId) returns Future(MaxImagesPerAvalanche)

      val result = subject.uploadImages("49fk349d", None, Some(adminUser), Seq.empty).resolve

      there was no(mockS3Service).uploadImage(any, any, any, any)
      there was no(mockDAL).insertAvalancheImage(any)
      result.header.status mustEqual 400
    }

//    "upload an image for a new report" in new WithApplication(appBuilder.build) {
//      mockDAL.countAvalancheImages(extId) returns Future(MaxImagesPerAvalanche - 3)
//
//      val requestWithUser = RequestWithUser(Some(AvyEyesUser), None, FakeRequest().withFileUpload("image", imageFile, "image/jpeg"))
//
//      val uploadAction = subject.upload("49fk349d", None)
//      val result: Result = call(uploadAction, requestWithUser).resolve
//
//      result.header.status mustEqual 401
//    }
  }

}

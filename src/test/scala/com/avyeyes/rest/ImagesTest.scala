package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.AvalancheImage
import com.avyeyes.service.{Injectors, AmazonS3ImageService, ResourceService}
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.util.Constants._
import net.liftweb.http._
import net.liftweb.mocks.MockHttpServletRequest
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

class ImagesTest extends WebSpec2 with AroundExample with Mockito {
  sequential

  val mockResources = mock[ResourceService]
  val mockAvalancheDal = mock[CachedDAL]
  val mockS3 = mock[AmazonS3ImageService]

  mockResources.getProperty("s3.imageBucket") returns "some-bucket"
  mockResources.getProperty("s3.fullaccess.accessKeyId") returns "3490griow"
  mockResources.getProperty("s3.fullaccess.secretAccessKey") returns "34ijgeij4"

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.dal.doWith(mockAvalancheDal) {
        Injectors.s3.doWith(mockS3) {
          AsResult(t)
        }
      }
    }

  val extId = "4jf93dkj"
  val goodImgFileName = "imgInDb"
  val goodImgMimeType = "image/jpeg"
  val avalancheImage = avalancheImageForTest.copy(avalanche = extId, filename = goodImgFileName, mimeType = goodImgMimeType)
  
  val badImgFileName = "imgNotInDb"
  val noImage: Option[AvalancheImage] = None
  
  mockAvalancheDal.getAvalancheImage(extId, goodImgFileName) returns Some(avalancheImage)
  mockAvalancheDal.getAvalancheImage(extId, badImgFileName) returns noImage

  "Image Post request" >> {
    isolated

    val mockPostRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId")
    mockPostRequest.method = "POST"
  
    "Insert a new image in the DB" withSFor(mockPostRequest) in {
      val images = new Images
      mockAvalancheDal.countAvalancheImages(any[String]) returns 0

      val filename = "testImgABC.jpg"
      val fileBytes = Array[Byte](10, 20, 30, 40, 50)
      val fph = FileParamHolder("Test Image", "image/jpeg", filename, fileBytes)

      val req = openLiftReqBox(S.request)
      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(images(reqWithFPH)())
        
      there was one(mockAvalancheDal).insertAvalancheImage(any[AvalancheImage])
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual extId
      extractJsonStringField(resp, "filename").length must beGreaterThan(0)
      extractJsonStringField(resp, "origFilename") mustEqual filename
      extractJsonLongField(resp, "size") mustEqual fileBytes.length
    }

    "Don't insert an image above the max images count" withSFor(mockPostRequest) in {
      val images = new Images
      mockAvalancheDal.countAvalancheImages(any[String]) returns MaxImagesPerAvalanche

      val fileName = "testImgABC"
      val fileBytes = Array[Byte](10, 20, 30, 40, 50)
      val fph = FileParamHolder("Test Image", "image/jpeg", fileName, fileBytes)

      val req = openLiftReqBox(S.request)
      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(images(reqWithFPH)())

      there was no(mockAvalancheDal).insertAvalancheImage(any[AvalancheImage])
      resp must beAnInstanceOf[ResponseWithReason]
    }
  }
  
  "Image Delete request" >> {
    val mockDeleteRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId/$goodImgFileName")
    mockDeleteRequest.method = "DELETE"

    "Delete an image from the DB" withSFor(mockDeleteRequest) in {
      val images = new Images
      val req = openLiftReqBox(S.request)

      val s3ExtIdArg = capture[String]
      val s3FilenameArg = capture[String]
      val dalExtIdArg = capture[String]
      val dalFilenameArg = capture[String]

      val resp = openLiftRespBox(images(req)())

      there was one(mockS3).deleteImage(s3ExtIdArg, s3FilenameArg)
      there was one(mockAvalancheDal).deleteAvalancheImage(dalExtIdArg, dalFilenameArg)
      resp must beAnInstanceOf[OkResponse]
      s3ExtIdArg.value mustEqual extId
      s3FilenameArg.value mustEqual goodImgFileName
      dalExtIdArg.value mustEqual extId
      dalFilenameArg.value mustEqual goodImgFileName
    }
  }
}

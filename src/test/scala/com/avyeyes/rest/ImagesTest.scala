package com.avyeyes.rest

import bootstrap.liftweb.Boot
import com.avyeyes.model.AvalancheImage
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.util.Constants._
import net.liftweb.http._
import net.liftweb.mocks.MockHttpServletRequest

class ImagesTest extends WebSpec2(Boot().boot _) with MockInjectors {
  sequential

  val extId = "4jf93dkj"
  val goodImgFileName = "imgInDb"
  val goodImgMimeType = "image/jpeg"
  val avalancheImage = avalancheImageForTest.copy(avyExtId = extId, filename = goodImgFileName, mimeType = goodImgMimeType)
  
  val badImgFileName = "imgNotInDb"
  val noImage: Option[AvalancheImage] = None
  
  mockAvalancheDal.getAvalancheImage(extId, goodImgFileName) returns Some(avalancheImage)
  mockAvalancheDal.getAvalancheImage(extId, badImgFileName) returns noImage

  "Image Post request" should {
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

      there was one(mockAvalancheDal).insertAvalancheImage(any[AvalancheImage]) // one interaction from the prev test
      resp must beAnInstanceOf[ResponseWithReason]
      resp.asInstanceOf[ResponseWithReason].reason must contain(MaxImagesPerAvalanche.toString)
    }
  }
  
  "Image Delete request" should {
    val mockDeleteRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId/$goodImgFileName")
    mockDeleteRequest.method = "DELETE"

    "Delete an image from the DB" withSFor(mockDeleteRequest) in {
      val images = new Images
      val req = openLiftReqBox(S.request)
     
      val extIdArg = capture[String]
      val filenameArg = capture[String]

      val resp = openLiftRespBox(images(req)())

      there was one(mockAvalancheDal).deleteAvalancheImage(extIdArg, filenameArg)
      resp must beAnInstanceOf[OkResponse]
      extIdArg.value mustEqual extId
      filenameArg.value mustEqual goodImgFileName
    }
  }
}

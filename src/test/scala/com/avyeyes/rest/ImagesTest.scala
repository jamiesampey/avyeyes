package com.avyeyes.rest

import bootstrap.liftweb.Boot
import com.avyeyes.model.AvalancheImage
import com.avyeyes.test._
import com.avyeyes.util.Constants._
import net.liftweb.http._
import net.liftweb.mocks.MockHttpServletRequest
import org.mockito.ArgumentCaptor

class ImagesTest extends WebSpec2(Boot().boot _) with MockInjectors with LiftHelpers {
  sequential

  val images = new Images

  val extId = "4jf93dkj"
  val goodImgFileName = "imgInDb"
  val goodImgMimeType = "image/jpeg"
  val goodImgBytes = Array[Byte](10, 20, 30, 40, 50, 60, 70)
  val avalancheImage = AvalancheImage(extId, goodImgFileName, goodImgMimeType, goodImgBytes.length, goodImgBytes)
  
  val badImgFileName = "imgNotInDb"
  val noImage: Option[AvalancheImage] = None
  
  mockAvalancheDao.selectAvalancheImage(extId, goodImgFileName) returns Some(avalancheImage)  
  mockAvalancheDao.selectAvalancheImage(extId, badImgFileName) returns noImage

  "Image Get request" should {
    "Return an image if it exists" withSFor(s"http://avyeyes.com/rest/images/$extId/$goodImgFileName") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(images(req)())
        
      resp must beAnInstanceOf[StreamingResponse]
      resp.asInstanceOf[StreamingResponse].size must_== goodImgBytes.length
      resp.asInstanceOf[StreamingResponse].headers must contain(("Content-Type", goodImgMimeType))
    }
    
    "Return a NotFoundResponse (404) if image does not exist" withSFor(s"http://avyeyes.com/rest/images/$extId/$badImgFileName") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(images(req)())
      
      resp must beAnInstanceOf[NotFoundResponse]
      resp.asInstanceOf[NotFoundResponse].message must contain(s"$extId/$badImgFileName")
    }
  }
  
  "Image Post request" should {
    val mockPostRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId")
    mockPostRequest.method = "POST"
  
    "Insert a new image in the DB" withSFor(mockPostRequest) in {
      mockAvalancheDao.countAvalancheImages(any[String]) returns 0

      val fileName = "testImgABC"
      val fileBytes = Array[Byte](10, 20, 30, 40, 50)
      val fph = FileParamHolder("Test Image", "image/jpeg", fileName, fileBytes)

      val req = openLiftReqBox(S.request)
      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(images(reqWithFPH)())
        
      there was one(mockAvalancheDao).insertAvalancheImage(any[AvalancheImage])
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") must_== extId
      extractJsonStringField(resp, "fileName") must_== fileName
      extractJsonLongField(resp, "fileSize") must_== fileBytes.length
    }

    "Don't insert an image above the max images count" withSFor(mockPostRequest) in {
      mockAvalancheDao.countAvalancheImages(any[String]) returns MaxImagesPerAvalanche

      val fileName = "testImgABC"
      val fileBytes = Array[Byte](10, 20, 30, 40, 50)
      val fph = FileParamHolder("Test Image", "image/jpeg", fileName, fileBytes)

      val req = openLiftReqBox(S.request)
      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(images(reqWithFPH)())

      there was one(mockAvalancheDao).insertAvalancheImage(any[AvalancheImage]) // one interaction from the prev test
      resp must beAnInstanceOf[ResponseWithReason]
      resp.asInstanceOf[ResponseWithReason].reason must contain(MaxImagesPerAvalanche.toString)
    }
  }
  
  "Image Delete request" should {
    val mockDeleteRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId/$goodImgFileName")
    mockDeleteRequest.method = "DELETE"
    
    "Delete an image from the DB" withSFor(mockDeleteRequest) in {
      val req = openLiftReqBox(S.request)
     
      val extIdArg: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String]);
      val filenameArg: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String]);

      val resp = openLiftRespBox(images(req)())

      there was one(mockAvalancheDao).deleteAvalancheImage(extIdArg.capture(), filenameArg.capture())
      resp must beAnInstanceOf[OkResponse]
      extIdArg.getValue must_== extId
      filenameArg.getValue must_== goodImgFileName
    }
  }
}

package com.avyeyes.rest

import com.avyeyes.model.enums._
import com.avyeyes.test._
import net.liftweb.http._
import net.liftweb.mocks.MockHttpServletRequest
import com.avyeyes.model.AvalancheImage

class ImagesTest extends WebSpec2 with MockPersistence with LiftHelpers {
  // Testing an OBJECT (singleton), so the mockAvalancheDao is inserted ONCE. 
  // Only one chance to mock all methods.

  val extId = "4jf93dkj"
  
  val goodImgFileName = "imgInDb"
  val goodImgMimeType = "image/jpeg"
  val goodImgBytes = Array[Byte](10, 20, 30, 40, 50, 60, 70)
  val avalancheImage = AvalancheImage(extId, goodImgFileName, goodImgMimeType, goodImgBytes)
  
  val badImgFileName = "imgNotInDb"
  val noImage: Option[AvalancheImage] = None
  
  mockAvalancheDao.selectAvalancheImage(extId, goodImgFileName) returns Some(avalancheImage)  
  mockAvalancheDao.selectAvalancheImage(extId, badImgFileName) returns noImage
      
  "Image Get request" should {
    "Return an image if it exists" withSFor(s"http://avyeyes.com/rest/images/$extId/$goodImgFileName") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(Images(req)())
        
      resp must beAnInstanceOf[StreamingResponse]
      resp.asInstanceOf[StreamingResponse].size must_== goodImgBytes.length
      resp.asInstanceOf[StreamingResponse].headers must contain(("Content-Type", goodImgMimeType))
    }
    
    "Return a NotFoundResponse (404) if image does not exist" withSFor(s"http://avyeyes.com/rest/images/$extId/$badImgFileName") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(Images(req)())
      
      resp must beAnInstanceOf[NotFoundResponse]
      resp.asInstanceOf[NotFoundResponse].message must contain(s"$extId/$badImgFileName")
    }
  }
  
  "Image Post request" should {
    val mockPostRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId")
    mockPostRequest.method = "POST"
  
    "Insert a new image in the DB" withSFor(mockPostRequest) in {
      val req = openLiftReqBox(S.request)
      
      val fileName = "testImgABC"
      val fileBytes = Array[Byte](10, 20, 30, 40, 50)
      val fph = FileParamHolder("Test Image", "image/jpeg", fileName, fileBytes)

      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(Images(reqWithFPH)())
        
      there was one(mockAvalancheDao).insertAvalancheImage(any[AvalancheImage])
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") must_== extId
      extractJsonStringField(resp, "fileName") must_== fileName
      extractJsonLongField(resp, "fileSize") must_== fileBytes.length
    }
  }
}

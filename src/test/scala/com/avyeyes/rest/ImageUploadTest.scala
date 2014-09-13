package com.avyeyes.rest

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.test._
import net.liftweb.http._
import net.liftweb.mocks.MockHttpServletRequest

class ImageUploadTest extends WebSpec2 with MockPersistence with LiftHelpers {
  val extId = "59fke4k0"
  val mockPostRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/imgupload/$extId")
  mockPostRequest.method = "POST"
  
  "Valid image upload REST request" should {
    "Insert a new image in the DB" withSFor(mockPostRequest) in {
      val req = openLiftReqBox(S.request)
      
      val fileName = "testImgABC"
      val fileBytes = Array[Byte](10, 20, 30, 40, 50)
      val fph = FileParamHolder("Test Image", "image/jpeg", fileName, fileBytes)

      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(ImageUpload(reqWithFPH)())
        
      there was one(mockAvalancheDao).insertAvalancheImage(any[AvalancheImg])
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") must_== extId
      extractJsonStringField(resp, "fileName") must_== fileName
      extractJsonLongField(resp, "fileSize") must_== fileBytes.length
    }
  }
  
}

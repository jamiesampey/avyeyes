package com.avyeyes.rest

import com.avyeyes.model.enums._
import com.avyeyes.test._
import net.liftweb.http._
import com.avyeyes.model.AvalancheImg

class ImageServeTest extends WebSpec2 with MockPersistence with LiftHelpers {
  // Testing an OBJECT (singleton), so the mockAvalancheDao is inserted ONCE. 
  // Only one chance to mock all methods.

  val extId = "4jf93dkj"
  
  val goodImgFileName = "imgInDb"
  val goodImgMimeType = "image/jpeg"
  val goodImgBytes = Array[Byte](10, 20, 30, 40, 50, 60, 70)
  val avalancheImage = AvalancheImg(extId, goodImgFileName, goodImgMimeType, goodImgBytes)
  
  val badImgFileName = "imgNotInDb"
  val noImage: Option[AvalancheImg] = None
  
  mockAvalancheDao.selectAvalancheImage(extId, goodImgFileName) returns Some(avalancheImage)  
  mockAvalancheDao.selectAvalancheImage(extId, badImgFileName) returns noImage
      
  "Valid avalanche image REST request" should {
    "Return an image if it exists" withSFor(s"http://avyeyes.com/rest/imgserve/$extId/$goodImgFileName") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(ImageServe(req)())
        
      resp must beAnInstanceOf[StreamingResponse]
      resp.asInstanceOf[StreamingResponse].size must_== goodImgBytes.length
      resp.asInstanceOf[StreamingResponse].headers must contain(("Content-Type", goodImgMimeType))
    }
    
    "Return a NotFoundResponse (404) if image does not exist" withSFor(s"http://avyeyes.com/rest/imgserve/$extId/$badImgFileName") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(ImageServe(req)())
      
      resp must beAnInstanceOf[NotFoundResponse]
      resp.asInstanceOf[NotFoundResponse].message must contain(s"$extId/$badImgFileName")
    }
  }
}

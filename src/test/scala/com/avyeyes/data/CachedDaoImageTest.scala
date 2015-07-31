package com.avyeyes.data

import com.avyeyes.test.Generators._
import com.avyeyes.util.UnauthorizedException
import org.specs2.mutable.Specification


class CachedDaoImageTest extends Specification with InMemoryDB {
  sequential

  val testAvalanche = avalancheForTest.copy(extId = "5j3fyjd9", viewable = false)
    
  "Avalanche Images" >> {
    val nonExistentAvalancheExtId = "594jk3i3"
    
    val img1Bytes = Array[Byte](10, 20, 30, 40, 50, 60, 70)
    val img1 = avalancheImageForTest.copy(avyExtId = testAvalanche.extId)
    val img2Bytes = Array[Byte](90, 80, 70)
    val img2 = avalancheImageForTest
  
    "Image insert and select works" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalanche(testAvalanche)
      dal.insertAvalancheImage(img1)
      val returnedImage = dal.getAvalancheImage(testAvalanche.extId, img1.filename).get
      
      returnedImage.avyExtId must_== testAvalanche.extId
      returnedImage.filename must_== img1.filename
      returnedImage.mimeType must_== img1.mimeType
    }
    
    "Image without corresponding avalanche is not selected" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalancheImage(img2)
      dal.getAvalancheImage(nonExistentAvalancheExtId, img2.filename) must_== None
    }
    
    "Image metadata search works" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalanche(testAvalanche)
      dal.insertAvalancheImage(img1)
      dal.insertAvalancheImage(img2)
      
      val returnedImages = dal.getAvalancheImages(testAvalanche.extId)
      returnedImages must have length(1)
      returnedImages.head must_== (img1.filename, img1.mimeType, img1.size)
    }
    
    "Image delete works for authorized session" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalanche(testAvalanche)
      dal.insertAvalancheImage(img1)
      
      dal.getAvalancheImage(testAvalanche.extId, img1.filename) must beSome
      dal.deleteAvalancheImage(testAvalanche.extId, img1.filename)
      dal.getAvalancheImage(testAvalanche.extId, img1.filename) must beNone
    }
    
    "Image delete does not work for unauthorized session" >> {
      mockUserSession.isAuthorizedSession() returns false

      dal.deleteAvalancheImage(testAvalanche.extId, img1.filename) must throwA[UnauthorizedException]
    }

    "Image count works" >> {
      mockUserSession.isAuthorizedSession() returns false

      dal.insertAvalancheImage(img1)
      dal.insertAvalancheImage(img2)
      dal.insertAvalancheImage(avalancheImageForTest.copy(avyExtId = img1.avyExtId))
      dal.countAvalancheImages(img1.avyExtId) must_== 2
      dal.countAvalancheImages(img2.avyExtId) must_== 1
    }

  }
}
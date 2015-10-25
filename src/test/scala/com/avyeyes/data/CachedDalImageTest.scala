package com.avyeyes.data

import com.avyeyes.service.UnauthorizedException
import com.avyeyes.test.Generators._
import org.specs2.mutable.Specification


class CachedDalImageTest extends Specification with InMemoryDB {
  sequential

  val testAvalanche = avalancheForTest.copy(extId = "5j3fyjd9", viewable = false)
    
  "Avalanche Images" >> {
    val nonExistentAvalancheExtId = "594jk3i3"
    
    val img1 = avalancheImageForTest.copy(avalanche = testAvalanche.extId)
    val img2 = avalancheImageForTest.copy(avalanche = nonExistentAvalancheExtId)
  
    "Image insert and select works" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalanche(testAvalanche)
      dal.insertAvalancheImage(img1)
      val returnedImage = dal.getAvalancheImage(testAvalanche.extId, img1.filename).get
      
      returnedImage.avalanche mustEqual testAvalanche.extId
      returnedImage.filename mustEqual img1.filename
      returnedImage.mimeType mustEqual img1.mimeType
    }
    
    "Image without corresponding avalanche is not selected" >> {
      mockUserSession.isAuthorizedSession() returns false

      dal.insertAvalancheImage(img2)
      dal.getAvalancheImage(nonExistentAvalancheExtId, img2.filename) mustEqual None
    }
    
    "Images select by avy extId only works" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalanche(testAvalanche)
      dal.insertAvalancheImage(img1)
      dal.insertAvalancheImage(img2)
      
      val returnedImages = dal.getAvalancheImages(testAvalanche.extId)
      returnedImages must have length(1)
      returnedImages.head mustEqual img1
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
      dal.insertAvalancheImage(avalancheImageForTest.copy(avalanche = img1.avalanche))
      dal.countAvalancheImages(img1.avalanche) mustEqual 2
      dal.countAvalancheImages(img2.avalanche) mustEqual 1
    }

  }
}
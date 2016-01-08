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
      returnedImage.caption mustEqual img1.caption
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

    "Image caption update works for authorized session" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalanche(testAvalanche)
      dal.insertAvalancheImage(img1)

      val testCaption = "look at this image!"
      dal.updateAvalancheImageCaption(testAvalanche.extId, img1.filename, Some(testCaption))
      val imageOpt = dal.getAvalancheImage(testAvalanche.extId, img1.filename)

      imageOpt.get.caption mustEqual Some(testCaption)
    }

    "Image caption remove works for authorized session" >> {
      mockUserSession.isAuthorizedSession() returns true

      dal.insertAvalanche(testAvalanche)
      dal.insertAvalancheImage(img1.copy(caption = Some("some caption text")))

      dal.updateAvalancheImageCaption(testAvalanche.extId, img1.filename, None)
      val imageOpt = dal.getAvalancheImage(testAvalanche.extId, img1.filename)

      imageOpt.get.caption must beNone
    }

    "Image caption update does not work for unauthorized session" >> {
      mockUserSession.isAuthorizedSession() returns false
      dal.updateAvalancheImageCaption(testAvalanche.extId, img1.filename, Some("some caption text")) must throwA[UnauthorizedException]
    }

    "Image order works for authorized session" >> {
      mockUserSession.isAuthorizedSession() returns true

      val order0 = avalancheImageForTest.copy(avalanche = testAvalanche.extId, sortOrder = 0)
      val order1 = avalancheImageForTest.copy(avalanche = testAvalanche.extId, sortOrder = 1)
      val order2 = avalancheImageForTest.copy(avalanche = testAvalanche.extId, sortOrder = 2)

      dal.insertAvalancheImage(order2)
      dal.insertAvalancheImage(order0)
      dal.insertAvalancheImage(order1)

      val firstImageList = dal.getAvalancheImages(testAvalanche.extId).map(_.filename)
      dal.updateAvalancheImageOrder(testAvalanche.extId, List(order1.filename, order2.filename, order0.filename))
      val secondImageList = dal.getAvalancheImages(testAvalanche.extId).map(_.filename)

      firstImageList mustEqual List(order0.filename, order1.filename, order2.filename)
      secondImageList mustEqual List(order1.filename, order2.filename, order0.filename)
    }

    "Image order does not work for unauthorized session" >> {
      mockUserSession.isAuthorizedSession() returns false
      dal.updateAvalancheImageOrder(testAvalanche.extId, List("a40ee710", "5d4e3a37")) must throwA[UnauthorizedException]
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
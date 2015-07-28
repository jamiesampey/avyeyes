package com.avyeyes.data

import com.avyeyes.model._
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
      val dao = new MemoryMapCachedDao(Authorized)

      dao.insertAvalanche(testAvalanche)
      dao.insertAvalancheImage(img1)
      val returnedImage = dao.getAvalancheImage(testAvalanche.extId, img1.filename).get
      
      returnedImage.avyExtId must_== testAvalanche.extId
      returnedImage.filename must_== img1.filename
      returnedImage.mimeType must_== img1.mimeType
    }
    
    "Image without corresponding avalanche is not selected" >> {
      val dao = new MemoryMapCachedDao(Authorized)

      dao.insertAvalancheImage(img2)
      dao.getAvalancheImage(nonExistentAvalancheExtId, img2.filename) must_== None
    }
    
    "Image metadata search works" >> {
      val dao = new MemoryMapCachedDao(Authorized)

      dao.insertAvalanche(testAvalanche)
      dao.insertAvalancheImage(img1)
      dao.insertAvalancheImage(img2)
      
      val returnedImages = dao.getAvalancheImages(testAvalanche.extId)
      returnedImages must have length(1)
      returnedImages.head must_== (img1.filename, img1.mimeType, img1.size)
    }
    
    "Image delete works for authorized session" >> {
      val dao = new MemoryMapCachedDao(Authorized)

      dao.insertAvalanche(testAvalanche)
      dao.insertAvalancheImage(img1)
      
      dao.getAvalancheImage(testAvalanche.extId, img1.filename) must beSome
      dao.deleteAvalancheImage(testAvalanche.extId, img1.filename)
      dao.getAvalancheImage(testAvalanche.extId, img1.filename) must beNone
    }
    
    "Image delete does not work for unauthorized session" >> {
      val dao = new MemoryMapCachedDao(NotAuthorized)

      dao.deleteAvalancheImage(testAvalanche.extId, img1.filename) must throwA[UnauthorizedException]
    }

    "Image count works" >> {
      val dao = new MemoryMapCachedDao(NotAuthorized)

      dao.insertAvalancheImage(img1)
      dao.insertAvalancheImage(img2)
      dao.insertAvalancheImage(avalancheImageForTest.copy(avyExtId = img1.avyExtId))
      dao.countAvalancheImages(img1.avyExtId) must_== 2
      dao.countAvalancheImages(img2.avyExtId) must_== 1
    }

  }
}
package com.avyeyes.data

import com.avyeyes.util.FutureOps._
import com.avyeyes.test.Generators._
import org.specs2.mutable.Specification
import scala.concurrent.Future

class CachedDalImageTest extends Specification with InMemoryDB {
  sequential

  val testAvalanche = avalancheForTest.copy(extId = "5j3fyjd9", viewable = false)
    
  "Avalanche Images" >> {
    val nonExistentAvalancheExtId = "594jk3i3"
    
    val img1 = avalancheImageForTest.copy(avalanche = testAvalanche.extId)
    val img2 = avalancheImageForTest.copy(avalanche = nonExistentAvalancheExtId)
  
    "Image insert and select works" >> {
      dal.insertAvalanche(testAvalanche).flatMap(_ => dal.insertAvalancheImage(img1)).resolve

      val returnedImage = dal.getAvalancheImage(testAvalanche.extId, img1.filename).resolve.get
      
      returnedImage.avalanche mustEqual testAvalanche.extId
      returnedImage.filename mustEqual img1.filename
      returnedImage.mimeType mustEqual img1.mimeType
      returnedImage.caption mustEqual img1.caption
    }
    
    "Images select by avy extId only works" >> {
      dal.insertAvalanche(testAvalanche).resolve
      dal.insertAvalancheImage(img1).resolve
      dal.insertAvalancheImage(img2).resolve
      
      val returnedImages = dal.getAvalancheImages(testAvalanche.extId).resolve
      returnedImages must haveLength(1)
      returnedImages.head mustEqual img1
    }

    "Image caption update works" >> {
      dal.insertAvalanche(testAvalanche).flatMap( _ => dal.insertAvalancheImage(img1)).resolve

      val testCaption = "look at this image!"
      dal.updateAvalancheImageCaption(testAvalanche.extId, img1.filename, Some(testCaption))
      val imageOpt = dal.getAvalancheImage(testAvalanche.extId, img1.filename).resolve

      imageOpt.get.caption mustEqual Some(testCaption)
    }

    "Image caption remove works" >> {
      dal.insertAvalanche(testAvalanche).flatMap( _ => dal.insertAvalancheImage(img1.copy(caption = Some("some caption text")))).resolve

      dal.updateAvalancheImageCaption(testAvalanche.extId, img1.filename, None).resolve
      val imageOpt = dal.getAvalancheImage(testAvalanche.extId, img1.filename).resolve

      imageOpt.get.caption must beNone
    }

    "Image order works" >> {
      val order0 = avalancheImageForTest.copy(avalanche = testAvalanche.extId, sortOrder = 0)
      val order1 = avalancheImageForTest.copy(avalanche = testAvalanche.extId, sortOrder = 1)
      val order2 = avalancheImageForTest.copy(avalanche = testAvalanche.extId, sortOrder = 2)

      Future.sequence(List(
        dal.insertAvalancheImage(order2),
        dal.insertAvalancheImage(order0),
        dal.insertAvalancheImage(order1)
      )).resolve

      val firstImageList = dal.getAvalancheImages(testAvalanche.extId).resolve.map(_.filename)
      dal.updateAvalancheImageOrder(testAvalanche.extId, List(order1.filename, order2.filename, order0.filename)).resolve
      val secondImageList = dal.getAvalancheImages(testAvalanche.extId).resolve.map(_.filename)

      firstImageList mustEqual List(order0.filename, order1.filename, order2.filename)
      secondImageList mustEqual List(order1.filename, order2.filename, order0.filename)
    }

    "Image delete works" >> {
      dal.insertAvalanche(testAvalanche).flatMap( _ => dal.insertAvalancheImage(img1)).resolve
      
      dal.getAvalancheImage(testAvalanche.extId, img1.filename).resolve must beSome
      dal.deleteAvalancheImage(testAvalanche.extId, img1.filename)
      dal.getAvalancheImage(testAvalanche.extId, img1.filename).resolve must beNone
    }
    
    "Image count works" >> {
      Future.sequence(List(dal.insertAvalancheImage(img1), dal.insertAvalancheImage(img2))).resolve
      dal.insertAvalancheImage(avalancheImageForTest.copy(avalanche = img1.avalanche)).resolve
      dal.countAvalancheImages(img1.avalanche).resolve mustEqual 2
      dal.countAvalancheImages(img2.avalanche).resolve mustEqual 1
    }

  }
}
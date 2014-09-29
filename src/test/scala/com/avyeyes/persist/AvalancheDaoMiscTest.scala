package com.avyeyes.persist

import org.specs2.mutable.Specification

import com.avyeyes.test._
import com.avyeyes.model._


class AvalancheDaoMiscTest extends Specification with InMemoryDB with AvalancheGenerator {
  sequential
  val dao = new SquerylAvalancheDao(() => true)

  val testAvalanche = avalancheAtLocation("5j3fyjd9", false, 43.57636345634, -100.5345550)
    
  "Avalanche insert" >> {
    "works" >> {
      dao insertAvalanche testAvalanche
      val readAvalanche = dao.selectAvalanche(testAvalanche.extId).get
      
      readAvalanche.extId must_== testAvalanche.extId
      readAvalanche.lat must_== testAvalanche.lat
      readAvalanche.lng must_== testAvalanche.lng
    }
  }
  
  "Avalanche Images" >> {
    val nonExistentAvalancheExtId = "594jk3i3"
    
    val img1Bytes = Array[Byte](10, 20, 30, 40, 50, 60, 70)
    val img1 = AvalancheImage(testAvalanche.extId, "imgInDb", "image/jpeg", img1Bytes.length, img1Bytes)
    val img2Bytes = Array[Byte](90, 80, 70)
    val img2 = AvalancheImage(nonExistentAvalancheExtId, "differentImg", "image/gif", img2Bytes.length, img2Bytes)
  
    "Image insert and select works" >> {
      dao insertAvalanche testAvalanche
      dao insertAvalancheImage img1
      val returnedImage = dao.selectAvalancheImage(testAvalanche.extId, img1.filename).get
      
      returnedImage.avyExtId must_== testAvalanche.extId
      returnedImage.filename must_== img1.filename
      returnedImage.mimeType must_== img1.mimeType
      returnedImage.bytes must_== img1.bytes
    }
    
    "Image without corresponding avalanche is not selected" >> {
      dao insertAvalancheImage img2
      dao.selectAvalancheImage(nonExistentAvalancheExtId, img2.filename) must_== None
    }
    
    "Avalanche image metadata search works" >> {
      dao insertAvalanche testAvalanche
      dao insertAvalancheImage img1
      dao insertAvalancheImage img2
      
      val returnedImages = dao.selectAvalancheImagesMetadata(testAvalanche.extId)
      returnedImages must have length(1)
      returnedImages.head must_== (img1.filename, img1.mimeType, img1.size)
    }
  }
}
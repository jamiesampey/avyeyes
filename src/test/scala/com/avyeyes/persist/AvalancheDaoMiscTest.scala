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
    
    val avalancheImg1 = AvalancheImg(testAvalanche.extId, "imgInDb", "image/jpeg", Array[Byte](10, 20, 30, 40, 50, 60, 70))
    val avalancheImg2 = AvalancheImg(nonExistentAvalancheExtId, "differentImg", "image/gif", Array[Byte](90, 80, 70))
  
    "Image insert and select works" >> {
      dao insertAvalanche testAvalanche
      dao insertAvalancheImage avalancheImg1
      val readImg = dao.selectAvalancheImage(testAvalanche.extId, avalancheImg1.filename).get
      
      readImg.avyExtId must_== testAvalanche.extId
      readImg.filename must_== avalancheImg1.filename
      readImg.mimeType must_== avalancheImg1.mimeType
      readImg.bytes must_== avalancheImg1.bytes
    }
    
    "Image without corresponding avalanche is not selected" >> {
      dao insertAvalancheImage avalancheImg2
      
      dao.selectAvalancheImage(nonExistentAvalancheExtId, avalancheImg2.filename) must_== None
    }
    
    "Avalanche image filename search works" >> {
      dao insertAvalancheImage avalancheImg1
      dao insertAvalancheImage avalancheImg2
      
      val imgFilenames = dao.selectAvalancheImageFilenames(testAvalanche.extId)
      imgFilenames must have length(1)
      imgFilenames.head must_== avalancheImg1.filename
    }
  }
}
package com.avyeyes.persist

import org.specs2.execute.Result
import org.specs2.mutable.Specification

import com.avyeyes.model.enums._
import com.avyeyes.test.AvalancheGenerator
import com.avyeyes.util.AEHelpers._

class AvalancheDaoAuthorizedSessionTest extends Specification with InMemoryDB with AvalancheGenerator {
  sequential
  
  val dao = new SquerylAvalancheDao(() => false) // unauthorized session

  val commonLat = 38.5763463456
  val commonLng = -102.5359593

  "Single avalanche select" >> {
    "viewable filter" >> {
      val nonviewableAvalanche = avalancheAtLocation("5j3fyjd9", false, commonLat, commonLng)
  
      dao insertAvalanche nonviewableAvalanche
      
      dao.selectAvalanche(nonviewableAvalanche.extId) must beNone
    }
  }
    
}
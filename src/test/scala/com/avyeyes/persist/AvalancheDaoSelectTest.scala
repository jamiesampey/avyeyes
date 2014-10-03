package com.avyeyes.persist

import org.specs2.execute.Result
import org.specs2.mutable.Specification

import com.avyeyes.model.enums._
import com.avyeyes.test.AvalancheGenerator
import com.avyeyes.util.AEHelpers._

class AvalancheDaoSelectTest extends Specification with InMemoryDB with AvalancheGenerator {
  sequential
  
  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val commonGeoBounds = GeoBounds((commonLat+.01).toString, (commonLng+.01).toString, (commonLat-.01).toString, (commonLng-.01).toString)
  
  "Unviewable avalanche select" >> {
    "Not allowed with unauthorized session" >> {
      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      val dao = new SquerylAvalancheDao(() => false)
      dao insertAvalanche nonviewableAvalanche
      dao.selectAvalanche(nonviewableAvalanche.extId) must beNone
    }
    
    "Allowed with authorized session" >> {
      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      val dao = new SquerylAvalancheDao(() => true)
      dao insertAvalanche nonviewableAvalanche
      dao.selectAvalanche(nonviewableAvalanche.extId) must beSome
    }
  }
    
  "Date filtering" >> {
    val jan1Avalanche = avalancheAtLocationOnDate("94jfi449", true, commonLat, commonLng, strToDate("01-01-2014"))
    val jan5Avalanche = avalancheAtLocationOnDate("42rtir54", true, commonLat, commonLng, strToDate("01-05-2014"))  
    val dao = new SquerylAvalancheDao(() => true)

    "From date filtering" >> {
      dao insertAvalanche jan1Avalanche
      dao insertAvalanche jan5Avalanche
      
      val fromDateCriteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "01-03-2014", "", "", "", "", "", "", "")
      
      verifySingleResult(dao, fromDateCriteria, jan5Avalanche.extId)
    }
    
    "To date filtering" >> {
      dao insertAvalanche jan1Avalanche
      dao insertAvalanche jan5Avalanche
      
      val toDateCriteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "01-03-2014", "", "", "", "", "", "")
      
      verifySingleResult(dao, toDateCriteria, jan1Avalanche.extId)
    }
    
    "Date filtering spanning year boundary" >> {
      dao insertAvalanche jan1Avalanche
      dao insertAvalanche jan5Avalanche
      
      val dateCriteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "12-25-2013", "01-04-2014", "", "", "", "", "", "")
      
      verifySingleResult(dao, dateCriteria, jan1Avalanche.extId)
    }
  }
  
  "Type/Trigger filtering" >> {
    val hsAsAvalanche = avalancheAtLocationWithTypeAndTrigger("943isfki", true, commonLat, commonLng, AvalancheType.HS, AvalancheTrigger.AS)
    val wsNeAvalanche = avalancheAtLocationWithTypeAndTrigger("m5ie56ko", true, commonLat, commonLng, AvalancheType.WS, AvalancheTrigger.NE)
    val dao = new SquerylAvalancheDao(() => true)
    
    "Type filtering" >> {
      dao insertAvalanche hsAsAvalanche
      dao insertAvalanche wsNeAvalanche
      
      val wsTypeCriteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", AvalancheType.WS.toString, "", "", "", "", "")
      
      verifySingleResult(dao, wsTypeCriteria, wsNeAvalanche.extId)
    }
    
    "Trigger filtering" >> {
      dao insertAvalanche hsAsAvalanche
      dao insertAvalanche wsNeAvalanche
      
      val asTriggerCriteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", "", AvalancheTrigger.AS.toString, "", "", "", "")
      
      verifySingleResult(dao, asTriggerCriteria, hsAsAvalanche.extId)
    }
  
    "Type and trigger filtering" >> {
      dao insertAvalanche hsAsAvalanche
      dao insertAvalanche wsNeAvalanche
      
      val hsAsCriteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", AvalancheType.HS.toString, AvalancheTrigger.AS.toString, "", "", "", "")
      
      verifySingleResult(dao, hsAsCriteria, hsAsAvalanche.extId)
    }
  }
  
  "R/D size filtering" >> {
    val r4d15Avalanche = avalancheAtLocationWithSize("94ik4of1", true, commonLat, commonLng, 4.0, 1.5)
    val r15d3Avalanche = avalancheAtLocationWithSize("43ufj4id", true, commonLat, commonLng, 1.5, 3.0)
    val dao = new SquerylAvalancheDao(() => true)
    
    "R size filtering" >> {
      dao insertAvalanche r4d15Avalanche
      dao insertAvalanche r15d3Avalanche
      
      val r4Criteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", "", "", "4.0", "", "", "")
        
      verifySingleResult(dao, r4Criteria, r4d15Avalanche.extId)
    }
    
    "D size filtering" >> {
      dao insertAvalanche r4d15Avalanche
      dao insertAvalanche r15d3Avalanche
      
      val d25Criteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", "", "", "", "2.5", "", "")
        
      verifySingleResult(dao, d25Criteria, r15d3Avalanche.extId)
    }
    
    "R and D size filtering" >> {
      dao insertAvalanche r4d15Avalanche
      dao insertAvalanche r15d3Avalanche
      
      val r3d1Criteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", "", "", "3.0", "1.0", "", "")
        
      verifySingleResult(dao, r3d1Criteria, r4d15Avalanche.extId)
    }
  }
  
  "Human numbers filtering" >> {
    val c4k0Avalanche = avalancheAtLocationWithCaughtKilledNumbers("349tgo94", true, commonLat, commonLng, 4, 0)
    val c3k2Avalanche = avalancheAtLocationWithCaughtKilledNumbers("e32fi417", true, commonLat, commonLng, 3, 2)
    val dao = new SquerylAvalancheDao(() => true)
    
    "Number caught filtering" >> {
      dao insertAvalanche c4k0Avalanche
      dao insertAvalanche c3k2Avalanche
      
      val c4Criteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", "", "", "", "", "4", "")
        
      verifySingleResult(dao, c4Criteria, c4k0Avalanche.extId)
    }
    
    "Number killed filtering" >> {
      dao insertAvalanche c4k0Avalanche
      dao insertAvalanche c3k2Avalanche
      
      val k1Criteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", "", "", "", "", "", "1")
        
      verifySingleResult(dao, k1Criteria, c3k2Avalanche.extId)
    }
    
    "Number caught and killed filtering" >> {
      dao insertAvalanche c4k0Avalanche
      dao insertAvalanche c3k2Avalanche
      
      val c2k2Criteria = AvalancheQuery(Some(true), Some(commonGeoBounds), "", "", "", "", "", "", "2", "2")
        
      verifySingleResult(dao, c2k2Criteria, c3k2Avalanche.extId)
    }
  }
  
  "Avalanche count" >> {
    "Counts avalanches by viewability" >> {
      val viewableAvalanche1 = avalancheAtLocation("94jfi449", true, commonLat, commonLng)
      val viewableAvalanche2 = avalancheAtLocation("42rtir54", true, commonLat, commonLng)
      val unviewableAvalanche = avalancheAtLocation("6903k2fh", false, commonLat, commonLng)
    
      val dao = new SquerylAvalancheDao(() => true)
      dao insertAvalanche viewableAvalanche1
      dao insertAvalanche viewableAvalanche2
      dao insertAvalanche unviewableAvalanche
      
      dao.countAvalanches(true) must_== 2
      dao.countAvalanches(false) must_==1
    }
  }
    
  private def verifySingleResult(dao: AvalancheDao, criteria: AvalancheQuery, extId: String): Result = {
    val resultList = dao.selectAvalanches(criteria)
    resultList must have length(1)
    resultList.head.extId must_== extId    
  }
}
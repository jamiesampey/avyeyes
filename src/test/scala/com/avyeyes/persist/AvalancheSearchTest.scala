package com.avyeyes.persist

import org.specs2.execute.Result
import org.specs2.mutable.Specification

import com.avyeyes.model.enums._
import com.avyeyes.test.AvalancheGenerator
import com.avyeyes.util.AEHelpers._

class AvalancheSearchTest extends Specification with InMemoryDB with AvalancheGenerator {
  sequential
  val dao = new SquerylAvalancheDao

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
    
  val viewableAvalanche = avalancheAtLocation("1j4ui3kr", true, commonLat, commonLng)
  val nonviewableAvalanche = avalancheAtLocation("5j3fyjd9", false, commonLat, commonLng)
    
  "Single avalanche select" >> {
    "viewable filter" >> {
      dao insertAvalanche nonviewableAvalanche
      
      val openSelectResult = dao.selectAvalanche(nonviewableAvalanche.extId)
      val viewableSelectResult = dao.selectViewableAvalanche(nonviewableAvalanche.extId)
      
      openSelectResult must beSome
      viewableSelectResult must beNone
    }
  }
  
  "Date filtering" >> {
    val jan1Avalanche = avalancheAtLocationOnDate("94jfi449", true, commonLat, commonLng, strToDate("01-01-2014"))
    val jan5Avalanche = avalancheAtLocationOnDate("42rtir54", true, commonLat, commonLng, strToDate("01-05-2014"))  
  
    "From date filtering" >> {
      dao insertAvalanche jan1Avalanche
      dao insertAvalanche jan5Avalanche
      
      val fromDateCriteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "01-03-2014", "", "", "", "", "", "", "")
      
      verifySingleResult(fromDateCriteria, jan5Avalanche.extId)
    }
    
    "To date filtering" >> {
      dao insertAvalanche jan1Avalanche
      dao insertAvalanche jan5Avalanche
      
      val toDateCriteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "01-03-2014", "", "", "", "", "", "")
      
      verifySingleResult(toDateCriteria, jan1Avalanche.extId)
    }
    
    "Date filtering spanning year boundary" >> {
      dao insertAvalanche jan1Avalanche
      dao insertAvalanche jan5Avalanche
      
      val dateCriteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "12-25-2013", "01-04-2014", "", "", "", "", "", "")
      
      verifySingleResult(dateCriteria, jan1Avalanche.extId)
    }
  }
  
  "Type/Trigger filtering" >> {
    val hsAsAvalanche = avalancheAtLocationWithTypeAndTrigger("943isfki", true, commonLat, commonLng, AvalancheType.HS, AvalancheTrigger.AS)
    val wsNeAvalanche = avalancheAtLocationWithTypeAndTrigger("m5ie56ko", true, commonLat, commonLng, AvalancheType.WS, AvalancheTrigger.NE)
        
    "Type filtering" >> {
      dao insertAvalanche hsAsAvalanche
      dao insertAvalanche wsNeAvalanche
      
      val wsTypeCriteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", AvalancheType.WS.toString, "", "", "", "", "")
      
      verifySingleResult(wsTypeCriteria, wsNeAvalanche.extId)
    }
    
    "Trigger filtering" >> {
      dao insertAvalanche hsAsAvalanche
      dao insertAvalanche wsNeAvalanche
      
      val asTriggerCriteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", "", AvalancheTrigger.AS.toString, "", "", "", "")
      
      verifySingleResult(asTriggerCriteria, hsAsAvalanche.extId)
    }
  
    "Type and trigger filtering" >> {
      dao insertAvalanche hsAsAvalanche
      dao insertAvalanche wsNeAvalanche
      
      val hsAsCriteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", AvalancheType.HS.toString, AvalancheTrigger.AS.toString, "", "", "", "")
      
      verifySingleResult(hsAsCriteria, hsAsAvalanche.extId)
    }
  }
  
  "R/D size filtering" >> {
    val r4d15Avalanche = avalancheAtLocationWithSize("94ik4of1", true, commonLat, commonLng, 4.0, 1.5)
    val r15d3Avalanche = avalancheAtLocationWithSize("43ufj4id", true, commonLat, commonLng, 1.5, 3.0)
    
    "R size filtering" >> {
      dao insertAvalanche r4d15Avalanche
      dao insertAvalanche r15d3Avalanche
      
      val r4Criteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", "", "", "4.0", "", "", "")
        
      verifySingleResult(r4Criteria, r4d15Avalanche.extId)
    }
    
    "D size filtering" >> {
      dao insertAvalanche r4d15Avalanche
      dao insertAvalanche r15d3Avalanche
      
      val d25Criteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", "", "", "", "2.5", "", "")
        
      verifySingleResult(d25Criteria, r15d3Avalanche.extId)
    }
    
    "R and D size filtering" >> {
      dao insertAvalanche r4d15Avalanche
      dao insertAvalanche r15d3Avalanche
      
      val r3d1Criteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", "", "", "3.0", "1.0", "", "")
        
      verifySingleResult(r3d1Criteria, r4d15Avalanche.extId)
    }
  }
  
  "Human numbers filtering" >> {
    val c4k0Avalanche = avalancheAtLocationWithCaughtKilledNumbers("349tgo94", true, commonLat, commonLng, 4, 0)
    val c3k2Avalanche = avalancheAtLocationWithCaughtKilledNumbers("e32fi417", true, commonLat, commonLng, 3, 2)
  
    "Number caught filtering" >> {
      dao insertAvalanche c4k0Avalanche
      dao insertAvalanche c3k2Avalanche
      
      val c4Criteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", "", "", "", "", "4", "")
        
      verifySingleResult(c4Criteria, c4k0Avalanche.extId)
    }
    
    "Number killed filtering" >> {
      dao insertAvalanche c4k0Avalanche
      dao insertAvalanche c3k2Avalanche
      
      val k1Criteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", "", "", "", "", "", "1")
        
      verifySingleResult(k1Criteria, c3k2Avalanche.extId)
    }
    
    "Number caught and killed filtering" >> {
      dao insertAvalanche c4k0Avalanche
      dao insertAvalanche c3k2Avalanche
      
      val c2k2Criteria = AvalancheSearchCriteria((commonLat+.01).toString, (commonLng+.01).toString, 
        (commonLat-.01).toString, (commonLng-.01).toString, "", "", "", "", "", "", "2", "2")
        
      verifySingleResult(c2k2Criteria, c3k2Avalanche.extId)
    }
  }
  
  private def verifySingleResult(criteria: AvalancheSearchCriteria, extId: String): Result = {
    val resultList = dao.selectAvalanches(criteria)
    resultList must have length(1)
    resultList.head.extId must_== extId    
  }
}
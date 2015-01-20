package com.avyeyes.persist

import org.specs2.execute.Result
import org.specs2.mutable.Specification
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvalancheQuery._
import com.avyeyes.test.AvalancheHelpers
import com.avyeyes.util.Helpers._
import org.joda.time.DateTime

class AvalancheDaoSelectTest extends Specification with InMemoryDB with AvalancheHelpers {
  sequential

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val commonGeoBounds = GeoBounds((commonLat+.01).toString, (commonLng+.01).toString, (commonLat-.01).toString, (commonLng-.01).toString)
  
  "Unviewable avalanche select" >> {
    "Not allowed with unauthorized session" >> {
      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      val dao = new SquerylAvalancheDao(() => false)
      insertTestAvalanche(dao, nonviewableAvalanche)
      dao.selectAvalanche(nonviewableAvalanche.extId) must beNone
    }
    
    "Allowed with authorized session" >> {
      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      val dao = new SquerylAvalancheDao(() => true)
      insertTestAvalanche(dao, nonviewableAvalanche)
      dao.selectAvalanche(nonviewableAvalanche.extId) must beSome
    }
  }
    
  "Date filtering" >> {
    val jan1Avalanche = avalancheAtLocationOnDate("94jfi449", true, commonLat, commonLng, strToDate("01-01-2014"))
    val jan5Avalanche = avalancheAtLocationOnDate("42rtir54", true, commonLat, commonLng, strToDate("01-05-2014"))  
    val dao = new SquerylAvalancheDao(() => true)

    "From date filtering" >> {
      insertTestAvalanche(dao, jan1Avalanche)
      insertTestAvalanche(dao, jan5Avalanche)
      
      val fromDateQuery = baseQuery.copy(fromDate = Some(strToDate("01-03-2014")))
      
      verifySingleResult(dao, fromDateQuery, jan5Avalanche.extId)
    }
    
    "To date filtering" >> {
      insertTestAvalanche(dao, jan1Avalanche)
      insertTestAvalanche(dao, jan5Avalanche)
      
      val toDateQuery = baseQuery.copy(toDate = Some(strToDate("01-03-2014")))
      
      verifySingleResult(dao, toDateQuery, jan1Avalanche.extId)
    }
    
    "Date filtering spanning year boundary" >> {
      insertTestAvalanche(dao, jan1Avalanche)
      insertTestAvalanche(dao, jan5Avalanche)
      
      val dateQuery = baseQuery.copy(fromDate = Some(strToDate("12-25-2013")), toDate = Some(strToDate("01-04-2014")))
      
      verifySingleResult(dao, dateQuery, jan1Avalanche.extId)
    }
  }
  
  "Type/Trigger filtering" >> {
    val hsAsAvalanche = avalancheAtLocationWithTypeAndTrigger("943isfki", true, commonLat, commonLng, AvalancheType.HS, AvalancheTrigger.AS)
    val wsNeAvalanche = avalancheAtLocationWithTypeAndTrigger("m5ie56ko", true, commonLat, commonLng, AvalancheType.WS, AvalancheTrigger.NE)
    val dao = new SquerylAvalancheDao(() => true)
    
    "Type filtering" >> {
      insertTestAvalanche(dao, hsAsAvalanche)
      insertTestAvalanche(dao, wsNeAvalanche)
      
      val wsTypeQuery = baseQuery.copy(avyType = Some(AvalancheType.WS))
      
      verifySingleResult(dao, wsTypeQuery, wsNeAvalanche.extId)
    }
    
    "Trigger filtering" >> {
      insertTestAvalanche(dao, hsAsAvalanche)
      insertTestAvalanche(dao, wsNeAvalanche)
      
      val asTriggerQuery = baseQuery.copy(avyTrigger = Some(AvalancheTrigger.AS))
      
      verifySingleResult(dao, asTriggerQuery, hsAsAvalanche.extId)
    }
  
    "Type and trigger filtering" >> {
      insertTestAvalanche(dao, hsAsAvalanche)
      insertTestAvalanche(dao, wsNeAvalanche)
      
      val hsAsQuery = baseQuery.copy(avyType = Some(AvalancheType.HS), avyTrigger = Some(AvalancheTrigger.AS))
      
      verifySingleResult(dao, hsAsQuery, hsAsAvalanche.extId)
    }
  }
  
  "R/D size filtering" >> {
    val r4d15Avalanche = avalancheAtLocationWithSize("94ik4of1", true, commonLat, commonLng, 4.0, 1.5)
    val r15d3Avalanche = avalancheAtLocationWithSize("43ufj4id", true, commonLat, commonLng, 1.5, 3.0)
    val dao = new SquerylAvalancheDao(() => true)
    
    "R size filtering" >> {
      insertTestAvalanche(dao, r4d15Avalanche)
      insertTestAvalanche(dao, r15d3Avalanche)
      
      val r4Query = baseQuery.copy(rSize = Some(4.0))
        
      verifySingleResult(dao, r4Query, r4d15Avalanche.extId)
    }
    
    "D size filtering" >> {
      insertTestAvalanche(dao, r4d15Avalanche)
      insertTestAvalanche(dao, r15d3Avalanche)
      
      val d25Query = baseQuery.copy(dSize = Some(2.5))
        
      verifySingleResult(dao, d25Query, r15d3Avalanche.extId)
    }
    
    "R and D size filtering" >> {
      insertTestAvalanche(dao, r4d15Avalanche)
      insertTestAvalanche(dao, r15d3Avalanche)
      
      val r3d1Query = baseQuery.copy(rSize = Some(3.0), dSize = Some(1.0))
        
      verifySingleResult(dao, r3d1Query, r4d15Avalanche.extId)
    }
  }
  
  "Human numbers filtering" >> {
    val c4k0Avalanche = avalancheAtLocationWithCaughtKilledNumbers("349tgo94", true, commonLat, commonLng, 4, 0)
    val c3k2Avalanche = avalancheAtLocationWithCaughtKilledNumbers("e32fi417", true, commonLat, commonLng, 3, 2)
    val dao = new SquerylAvalancheDao(() => true)
    
    "Number caught filtering" >> {
      insertTestAvalanche(dao, c4k0Avalanche)
      insertTestAvalanche(dao, c3k2Avalanche)
      
      val c4Query = baseQuery.copy(numCaught = Some(4))
        
      verifySingleResult(dao, c4Query, c4k0Avalanche.extId)
    }
    
    "Number killed filtering" >> {
      insertTestAvalanche(dao, c4k0Avalanche)
      insertTestAvalanche(dao, c3k2Avalanche)
      
      val k1Query = baseQuery.copy(numKilled = Some(1))
        
      verifySingleResult(dao, k1Query, c3k2Avalanche.extId)
    }
    
    "Number caught and killed filtering" >> {
      insertTestAvalanche(dao, c4k0Avalanche)
      insertTestAvalanche(dao, c3k2Avalanche)
      
      val c2k2Query = baseQuery.copy(numCaught = Some(2), numKilled = Some(2))
        
      verifySingleResult(dao, c2k2Query, c3k2Avalanche.extId)
    }
  }
  
  "Avalanche count" >> {
    "Counts avalanches by viewability" >> {
      val viewableAvalanche1 = avalancheAtLocation("94jfi449", true, commonLat, commonLng)
      val viewableAvalanche2 = avalancheAtLocation("42rtir54", true, commonLat, commonLng)
      val unviewableAvalanche = avalancheAtLocation("6903k2fh", false, commonLat, commonLng)
    
      val dao = new SquerylAvalancheDao(() => true)
      insertTestAvalanche(dao, viewableAvalanche1)
      insertTestAvalanche(dao, viewableAvalanche2)
      insertTestAvalanche(dao, unviewableAvalanche)
      
      dao.countAvalanches(true) must_== 2
      dao.countAvalanches(false) must_==1
    }
  }
  
  "Ordering" >> {
    val latest = avalancheAtLocationOnDate("94jfi449", false, commonLat, commonLng, new DateTime(System.currentTimeMillis).toDate)
    val earliest = avalancheAtLocationOnDate("42rtir54", false, commonLat, commonLng, new DateTime(System.currentTimeMillis).minusDays(20).toDate)
    val middle = avalancheAtLocationOnDate("6903k2fh", false, commonLat, commonLng, new DateTime(System.currentTimeMillis).minusDays(10).toDate)

    val dao = new SquerylAvalancheDao(() => true)
    
    "Selects can be ordered by date ascending" >> {
      insertTestAvalanche(dao, latest)
      insertTestAvalanche(dao, earliest)
      insertTestAvalanche(dao, middle)
      
      val dateAscOrderQuery = baseQuery.copy(orderBy = List((OrderField.AvyDate, OrderDirection.Asc)))
      val avyDateAscArray = dao.selectAvalanches(dateAscOrderQuery).toArray
      
      avyDateAscArray(0).extId must_== earliest.extId
      avyDateAscArray(1).extId must_== middle.extId
      avyDateAscArray(2).extId must_== latest.extId
    }
    
    "Selects can be ordered by date descending" >> {
      insertTestAvalanche(dao, latest)
      insertTestAvalanche(dao, earliest)
      insertTestAvalanche(dao, middle)

      val dateDescOrderQuery = baseQuery.copy(orderBy = List((OrderField.AvyDate, OrderDirection.Desc)))
      val avyDateDescArray = dao.selectAvalanches(dateDescOrderQuery).toArray
      
      avyDateDescArray(0).extId must_== latest.extId
      avyDateDescArray(1).extId must_== middle.extId
      avyDateDescArray(2).extId must_== earliest.extId
    }
  }
  
  "Pagination" >> {
    val avalanche1 = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
    val avalanche2 = avalancheAtLocation("42rtir54", false, commonLat, commonLng)
    val avalanche3 = avalancheAtLocation("6903k2fh", false, commonLat, commonLng)
    
    val dao = new SquerylAvalancheDao(() => true)
    
    "Selects can be paginated" >> {
      insertTestAvalanche(dao, avalanche1)
      insertTestAvalanche(dao, avalanche2)
      insertTestAvalanche(dao, avalanche3)
                  
      val firstPageQuery = baseQuery.copy(offset = 0, limit = 2)
      val secondPageQuery = baseQuery.copy(offset = 2, limit = 2)
      
      dao.selectAvalanches(firstPageQuery).size must_== 2
      dao.selectAvalanches(secondPageQuery).size must_== 1
    }
  }
  
  private def verifySingleResult(dao: AvalancheDao, query: AvalancheQuery, extId: String): Result = {
    val resultList = dao.selectAvalanches(query)
    resultList must have length(1)
    resultList.head.extId must_== extId    
  }
}
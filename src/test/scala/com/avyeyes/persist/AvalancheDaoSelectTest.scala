package com.avyeyes.persist

import com.avyeyes.data._
import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist
import com.avyeyes.test.AvalancheHelpers
import com.avyeyes.util.Helpers._
import org.joda.time.DateTime
import org.specs2.execute.Result
import org.specs2.mutable.Specification

class AvalancheDaoSelectTest extends Specification with InMemoryDB with AvalancheHelpers {
  sequential

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val commonGeoBounds = GeoBounds(commonLat+.01, commonLng+.01, commonLat-.01, commonLng-.01)
  
  "Unviewable avalanche select" >> {
    "Not allowed with unauthorized session" >> {
      val dao = new SquerylAvalancheDao(NotAuthorized)

      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      insertTestAvalanche(dao, nonviewableAvalanche)

      dao.selectAvalanche(nonviewableAvalanche.extId) must beNone
    }
    
    "Allowed with authorized session" >> {
      val dao = new SquerylAvalancheDao(Authorized)

      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      insertTestAvalanche(dao, nonviewableAvalanche)

      dao.selectAvalanche(nonviewableAvalanche.extId) must beSome
    }
  }
    
  "Date filtering" >> {
    val dao = new SquerylAvalancheDao(Authorized)

    val jan1Avalanche = avalancheOnDate("94jfi449", true, commonLat, commonLng, strToDate("01-01-2014"))
    val jan5Avalanche = avalancheOnDate("42rtir54", true, commonLat, commonLng, strToDate("01-05-2014"))

    "From date filtering" >> {
      insertTestAvalanche(dao, jan1Avalanche)
      insertTestAvalanche(dao, jan5Avalanche)
      
      val fromDateQuery = AvalancheQuery(fromDate = Some(strToDate("01-03-2014")))
      
      verifySingleResult(dao, fromDateQuery, jan5Avalanche.extId)
    }
    
    "To date filtering" >> {
      insertTestAvalanche(dao, jan1Avalanche)
      insertTestAvalanche(dao, jan5Avalanche)
      
      val toDateQuery = AvalancheQuery(toDate = Some(strToDate("01-03-2014")))
      
      verifySingleResult(dao, toDateQuery, jan1Avalanche.extId)
    }
    
    "Date filtering spanning year boundary" >> {
      insertTestAvalanche(dao, jan1Avalanche)
      insertTestAvalanche(dao, jan5Avalanche)
      
      val dateQuery = AvalancheQuery(fromDate = Some(strToDate("12-25-2013")), toDate = Some(strToDate("01-04-2014")))
      
      verifySingleResult(dao, dateQuery, jan1Avalanche.extId)
    }
  }
  
  "Type/Trigger filtering" >> {
    val dao = new SquerylAvalancheDao(Authorized)

    val hsAsAvalanche = avalancheWithTypeAndTrigger("943isfki", true, commonLat, commonLng, AvalancheType.HS, AvalancheTrigger.AS)
    val wsNeAvalanche = avalancheWithTypeAndTrigger("m5ie56ko", true, commonLat, commonLng, AvalancheType.WS, AvalancheTrigger.NE)

    "Type filtering" >> {
      insertTestAvalanche(dao, hsAsAvalanche)
      insertTestAvalanche(dao, wsNeAvalanche)
      
      val wsTypeQuery = AvalancheQuery(avyType = Some(AvalancheType.WS))
      
      verifySingleResult(dao, wsTypeQuery, wsNeAvalanche.extId)
    }
    
    "Trigger filtering" >> {
      insertTestAvalanche(dao, hsAsAvalanche)
      insertTestAvalanche(dao, wsNeAvalanche)
      
      val asTriggerQuery = AvalancheQuery(trigger = Some(AvalancheTrigger.AS))
      
      verifySingleResult(dao, asTriggerQuery, hsAsAvalanche.extId)
    }
  
    "Type and trigger filtering" >> {
      insertTestAvalanche(dao, hsAsAvalanche)
      insertTestAvalanche(dao, wsNeAvalanche)
      
      val hsAsQuery = AvalancheQuery(avyType = Some(AvalancheType.HS), trigger = Some(AvalancheTrigger.AS))
      
      verifySingleResult(dao, hsAsQuery, hsAsAvalanche.extId)
    }
  }
  
  "R/D size filtering" >> {
    val dao = new SquerylAvalancheDao(Authorized)

    val r4d15Avalanche = avalancheWithSize("94ik4of1", true, commonLat, commonLng, 4.0, 1.5)
    val r15d3Avalanche = avalancheWithSize("43ufj4id", true, commonLat, commonLng, 1.5, 3.0)

    "R size filtering" >> {
      insertTestAvalanche(dao, r4d15Avalanche)
      insertTestAvalanche(dao, r15d3Avalanche)
      
      val r4Query = AvalancheQuery(rSize = Some(4.0))
        
      verifySingleResult(dao, r4Query, r4d15Avalanche.extId)
    }
    
    "D size filtering" >> {
      insertTestAvalanche(dao, r4d15Avalanche)
      insertTestAvalanche(dao, r15d3Avalanche)
      
      val d25Query = AvalancheQuery(dSize = Some(2.5))
        
      verifySingleResult(dao, d25Query, r15d3Avalanche.extId)
    }
    
    "R and D size filtering" >> {
      insertTestAvalanche(dao, r4d15Avalanche)
      insertTestAvalanche(dao, r15d3Avalanche)
      
      val r3d1Query = AvalancheQuery(rSize = Some(3.0), dSize = Some(1.0))
        
      verifySingleResult(dao, r3d1Query, r4d15Avalanche.extId)
    }
  }
  
  "Human numbers filtering" >> {
    val dao = new SquerylAvalancheDao(Authorized)

    val c4k0Avalanche = avalancheWithCaughtKilledNumbers("349tgo94", true, commonLat, commonLng, 4, 0)
    val c3k2Avalanche = avalancheWithCaughtKilledNumbers("e32fi417", true, commonLat, commonLng, 3, 2)

    "Number caught filtering" >> {
      insertTestAvalanche(dao, c4k0Avalanche)
      insertTestAvalanche(dao, c3k2Avalanche)
      
      val c4Query = AvalancheQuery(numCaught = Some(4))
        
      verifySingleResult(dao, c4Query, c4k0Avalanche.extId)
    }
    
    "Number killed filtering" >> {
      insertTestAvalanche(dao, c4k0Avalanche)
      insertTestAvalanche(dao, c3k2Avalanche)
      
      val k1Query = AvalancheQuery(numKilled = Some(1))
        
      verifySingleResult(dao, k1Query, c3k2Avalanche.extId)
    }
    
    "Number caught and killed filtering" >> {
      insertTestAvalanche(dao, c4k0Avalanche)
      insertTestAvalanche(dao, c3k2Avalanche)
      
      val c2k2Query = AvalancheQuery(numCaught = Some(2), numKilled = Some(2))
        
      verifySingleResult(dao, c2k2Query, c3k2Avalanche.extId)
    }
  }
  
  "Avalanche count" >> {
    "Counts avalanches by viewability" >> {
      val dao = new SquerylAvalancheDao(Authorized)

      val viewableAvalanche1 = avalancheAtLocation("94jfi449", true, commonLat, commonLng)
      val viewableAvalanche2 = avalancheAtLocation("42rtir54", true, commonLat, commonLng)
      val unviewableAvalanche = avalancheAtLocation("6903k2fh", false, commonLat, commonLng)
    
      insertTestAvalanche(dao, viewableAvalanche1)
      insertTestAvalanche(dao, viewableAvalanche2)
      insertTestAvalanche(dao, unviewableAvalanche)
      
      dao.countAvalanches(Some(true)) must_== 2
      dao.countAvalanches(Some(false)) must_==1
      dao.countAvalanches(None) must_==3
    }
  }
  
  "Ordering" >> {
    val dao = new SquerylAvalancheDao(Authorized)

    val now = DateTime.now
    val latest = avalancheOnDate("94jfi449", false, commonLat, commonLng, now)
    val earliest = avalancheOnDate("42rtir54", false, commonLat, commonLng, now.minusDays(20))
    val middle = avalancheOnDate("6903k2fh", false, commonLat, commonLng, now.minusDays(10))

    "Selects can be ordered by date ascending" >> {
      insertTestAvalanche(dao, latest)
      insertTestAvalanche(dao, earliest)
      insertTestAvalanche(dao, middle)
      
      val dateAscOrderQuery = AvalancheQuery(orderBy = List((OrderField.avyDate, OrderDirection.asc)))
      val avyDateAscArray = dao.selectAvalanches(dateAscOrderQuery).toArray
      
      avyDateAscArray(0).extId must_== earliest.extId
      avyDateAscArray(1).extId must_== middle.extId
      avyDateAscArray(2).extId must_== latest.extId
    }
    
    "Selects can be ordered by date descending" >> {
      insertTestAvalanche(dao, latest)
      insertTestAvalanche(dao, earliest)
      insertTestAvalanche(dao, middle)

      val dateDescOrderQuery = AvalancheQuery(orderBy = List((OrderField.avyDate, OrderDirection.desc)))
      val avyDateDescArray = dao.selectAvalanches(dateDescOrderQuery).toArray
      
      avyDateDescArray(0).extId must_== latest.extId
      avyDateDescArray(1).extId must_== middle.extId
      avyDateDescArray(2).extId must_== earliest.extId
    }
  }
  
  "Pagination" >> {
    val dao = new SquerylAvalancheDao(Authorized)

    val avalanche1 = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
    val avalanche2 = avalancheAtLocation("42rtir54", false, commonLat, commonLng)
    val avalanche3 = avalancheAtLocation("6903k2fh", false, commonLat, commonLng)

    "Selects can be paginated" >> {
      insertTestAvalanche(dao, avalanche1)
      insertTestAvalanche(dao, avalanche2)
      insertTestAvalanche(dao, avalanche3)
                  
      val firstPageQuery = AvalancheQuery(offset = 0, limit = 2)
      val secondPageQuery = AvalancheQuery(offset = 2, limit = 2)
      
      dao.selectAvalanches(firstPageQuery).size must_== 2
      dao.selectAvalanches(secondPageQuery).size must_== 1
    }
  }
  
  private def verifySingleResult(dao: CachedDao, query: AvalancheQuery, extId: String): Result = {
    val resultList = dao.selectAvalanches(query)
    resultList must have length(1)
    resultList.head.extId must_== extId    
  }
}
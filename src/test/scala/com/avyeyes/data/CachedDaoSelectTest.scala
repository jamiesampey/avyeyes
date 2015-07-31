package com.avyeyes.data

import com.avyeyes.test.Generators._
import com.avyeyes.model.enums._
import com.avyeyes.util.Helpers._
import org.joda.time.DateTime
import org.specs2.execute.Result
import org.specs2.mutable.Specification

class CachedDaoSelectTest extends Specification with InMemoryDB {
  sequential

  "Unviewable avalanche select" >> {
    val nonviewableAvalanche = avalancheForTest.copy(viewable = false)

    "Not allowed with unauthorized session" >> {
      val dao = memoryMapCachedDaoForTest(NotAuthorized)
      dao.insertAvalanche(nonviewableAvalanche)
      dao.getAvalanche(nonviewableAvalanche.extId) must beNone
    }
    
    "Allowed with authorized session" >> {
      val dao = memoryMapCachedDaoForTest(Authorized)
      dao.insertAvalanche(nonviewableAvalanche)
      dao.getAvalanche(nonviewableAvalanche.extId) must beSome
    }
  }
    
  "Date filtering" >> {
    val dao = memoryMapCachedDaoForTest(Authorized)

    val jan1Avalanche = avalancheForTest.copy(viewable = true, date = strToDate("01-01-2014"))
    val jan5Avalanche = avalancheForTest.copy(viewable = true, date = strToDate("01-05-2014"))

    "From date filtering" >> {
      dao.insertAvalanche(jan1Avalanche)
      dao.insertAvalanche(jan5Avalanche)
      val fromDateQuery = AvalancheQuery(fromDate = Some(strToDate("01-03-2014")))
      verifySingleResult(dao, fromDateQuery, jan5Avalanche.extId)
    }
    
    "To date filtering" >> {
      dao.insertAvalanche(jan1Avalanche)
      dao.insertAvalanche(jan5Avalanche)
      val toDateQuery = AvalancheQuery(toDate = Some(strToDate("01-03-2014")))
      verifySingleResult(dao, toDateQuery, jan1Avalanche.extId)
    }
    
    "Date filtering spanning year boundary" >> {
      dao.insertAvalanche(jan1Avalanche)
      dao.insertAvalanche(jan5Avalanche)
      val dateQuery = AvalancheQuery(fromDate = Some(strToDate("12-25-2013")), toDate = Some(strToDate("01-04-2014")))
      verifySingleResult(dao, dateQuery, jan1Avalanche.extId)
    }
  }
  
  "Type/Trigger filtering" >> {
    val dao = memoryMapCachedDaoForTest(Authorized)

    val hsAsAvalanche = avalancheForTest.copy(viewable = true, classification =
      genClassification.sample.get.copy(avyType = AvalancheType.HS, trigger = AvalancheTrigger.AS))
    val wsNeAvalanche = avalancheForTest.copy(viewable = true, classification =
      genClassification.sample.get.copy(avyType = AvalancheType.WS, trigger = AvalancheTrigger.NE))

    "Type filtering" >> {
      dao.insertAvalanche(hsAsAvalanche)
      dao.insertAvalanche(wsNeAvalanche)
      val wsTypeQuery = AvalancheQuery(avyType = Some(AvalancheType.WS))
      verifySingleResult(dao, wsTypeQuery, wsNeAvalanche.extId)
    }
    
    "Trigger filtering" >> {
      dao.insertAvalanche(hsAsAvalanche)
      dao.insertAvalanche(wsNeAvalanche)
      val asTriggerQuery = AvalancheQuery(trigger = Some(AvalancheTrigger.AS))
      verifySingleResult(dao, asTriggerQuery, hsAsAvalanche.extId)
    }
  
    "Type and trigger filtering" >> {
      dao.insertAvalanche(hsAsAvalanche)
      dao.insertAvalanche(wsNeAvalanche)
      val hsAsQuery = AvalancheQuery(avyType = Some(AvalancheType.HS), trigger = Some(AvalancheTrigger.AS))
      verifySingleResult(dao, hsAsQuery, hsAsAvalanche.extId)
    }
  }
  
  "R/D size filtering" >> {
    val dao = memoryMapCachedDaoForTest(Authorized)

    val r4d15Avalanche = avalancheForTest.copy(viewable = true, classification =
      genClassification.sample.get.copy(rSize = 4.0, dSize = 1.5))
    val r15d3Avalanche = avalancheForTest.copy(viewable = true, classification =
      genClassification.sample.get.copy(rSize = 1.5, dSize = 3.0))

    "R size filtering" >> {
      dao.insertAvalanche(r4d15Avalanche)
      dao.insertAvalanche(r15d3Avalanche)
      val r4Query = AvalancheQuery(rSize = Some(4.0))
      verifySingleResult(dao, r4Query, r4d15Avalanche.extId)
    }
    
    "D size filtering" >> {
      dao.insertAvalanche(r4d15Avalanche)
      dao.insertAvalanche(r15d3Avalanche)
      val d25Query = AvalancheQuery(dSize = Some(2.5))
      verifySingleResult(dao, d25Query, r15d3Avalanche.extId)
    }
    
    "R and D size filtering" >> {
      dao.insertAvalanche(r4d15Avalanche)
      dao.insertAvalanche(r15d3Avalanche)
      val r3d1Query = AvalancheQuery(rSize = Some(3.0), dSize = Some(1.0))
      verifySingleResult(dao, r3d1Query, r4d15Avalanche.extId)
    }
  }
  
  "Human numbers filtering" >> {
    val dao = memoryMapCachedDaoForTest(Authorized)

    val c4k0Avalanche = avalancheForTest.copy(viewable = true,
      humanNumbers = genHumanNumbers.sample.get.copy(caught = 4, killed = 0))
    val c3k2Avalanche = avalancheForTest.copy(viewable = true,
      humanNumbers = genHumanNumbers.sample.get.copy(caught = 3, killed = 2))

    "Number caught filtering" >> {
      dao.insertAvalanche(c4k0Avalanche)
      dao.insertAvalanche(c3k2Avalanche)
      val c4Query = AvalancheQuery(numCaught = Some(4))
      verifySingleResult(dao, c4Query, c4k0Avalanche.extId)
    }
    
    "Number killed filtering" >> {
      dao.insertAvalanche(c4k0Avalanche)
      dao.insertAvalanche(c3k2Avalanche)
      val k1Query = AvalancheQuery(numKilled = Some(1))
      verifySingleResult(dao, k1Query, c3k2Avalanche.extId)
    }
    
    "Number caught and killed filtering" >> {
      dao.insertAvalanche(c4k0Avalanche)
      dao.insertAvalanche(c3k2Avalanche)
      val c2k2Query = AvalancheQuery(numCaught = Some(2), numKilled = Some(2))
      verifySingleResult(dao, c2k2Query, c3k2Avalanche.extId)
    }
  }
  
  "Avalanche count" >> {
    "Counts avalanches by viewability" >> {
      val dao = memoryMapCachedDaoForTest(Authorized)

      val viewableAvalanche1 = avalancheForTest.copy(viewable = true)
      val viewableAvalanche2 = avalancheForTest.copy(viewable = true)
      val unviewableAvalanche = avalancheForTest.copy(viewable = false)
    
      dao.insertAvalanche(viewableAvalanche1)
      dao.insertAvalanche(viewableAvalanche2)
      dao.insertAvalanche(unviewableAvalanche)
      
      dao.countAvalanches(Some(true)) must_== 2
      dao.countAvalanches(Some(false)) must_==1
      dao.countAvalanches(None) must_==3
    }
  }
  
  "Ordering" >> {
    val dao = memoryMapCachedDaoForTest(Authorized)

    val now = DateTime.now
    val latest = avalancheForTest.copy(viewable = false, date = now)
    val earliest = avalancheForTest.copy(viewable = false, date = now.minusDays(20))
    val middle = avalancheForTest.copy(viewable = false, date = now.minusDays(10))

    "Selects can be ordered by date ascending" >> {
      dao.insertAvalanche(latest)
      dao.insertAvalanche(earliest)
      dao.insertAvalanche(middle)
      
      val dateAscOrderQuery = AvalancheQuery(order = List((OrderField.Date, OrderDirection.asc)))
      val avyDateAscArray = dao.getAvalanches(dateAscOrderQuery).toArray
      
      avyDateAscArray(0).extId must_== earliest.extId
      avyDateAscArray(1).extId must_== middle.extId
      avyDateAscArray(2).extId must_== latest.extId
    }
    
    "Selects can be ordered by date descending" >> {
      dao.insertAvalanche(latest)
      dao.insertAvalanche(earliest)
      dao.insertAvalanche(middle)

      val dateDescOrderQuery = AvalancheQuery(order = List((OrderField.Date, OrderDirection.desc)))
      val avyDateDescArray = dao.getAvalanches(dateDescOrderQuery).toArray
      
      avyDateDescArray(0).extId must_== latest.extId
      avyDateDescArray(1).extId must_== middle.extId
      avyDateDescArray(2).extId must_== earliest.extId
    }
  }
  
  "Pagination" >> {
    val dao = memoryMapCachedDaoForTest(Authorized)

    "Selects can be paginated" >> {
      dao.insertAvalanche(avalancheForTest)
      dao.insertAvalanche(avalancheForTest)
      dao.insertAvalanche(avalancheForTest)
                  
      val firstPageQuery = AvalancheQuery(offset = 0, limit = 2)
      val secondPageQuery = AvalancheQuery(offset = 2, limit = 2)
      
      dao.getAvalanches(firstPageQuery).size must_== 2
      dao.getAvalanches(secondPageQuery).size must_== 1
    }
  }
  
  private def verifySingleResult(dao: CachedDAL, query: AvalancheQuery, extId: String): Result = {
    val resultList = dao.getAvalanches(query)
    resultList must have length(1)
    resultList.head.extId must_== extId    
  }
}
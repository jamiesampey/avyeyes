package com.avyeyes.data

import com.avyeyes.model.enums._
import helpers.Generators._
import com.avyeyes.util.Converters._
import org.joda.time.DateTime
import org.specs2.execute.Result
import org.specs2.mutable.Specification

class CachedDalSelectTest extends Specification {
//  sequential
//
//  "Date filtering" >> {
//    val jan1Avalanche = avalancheForTest.copy(viewable = true, date = strToDate("01-01-2014"))
//    val jan5Avalanche = avalancheForTest.copy(viewable = true, date = strToDate("01-05-2014"))
//
//    "From date filtering" >> {
//      insertAvalanches(jan1Avalanche, jan5Avalanche)
//      val fromDateQuery = AvalancheQuery(fromDate = Some(strToDate("01-03-2014")))
//      verifySingleResult(dal, fromDateQuery, jan5Avalanche.extId)
//    }
//
//    "To date filtering" >> {
//      insertAvalanches(jan1Avalanche, jan5Avalanche)
//      val toDateQuery = AvalancheQuery(toDate = Some(strToDate("01-03-2014")))
//      verifySingleResult(dal, toDateQuery, jan1Avalanche.extId)
//    }
//
//    "Date filtering spanning year boundary" >> {
//      insertAvalanches(jan1Avalanche, jan5Avalanche)
//      val dateQuery = AvalancheQuery(fromDate = Some(strToDate("12-25-2013")), toDate = Some(strToDate("01-04-2014")))
//      verifySingleResult(dal, dateQuery, jan1Avalanche.extId)
//    }
//  }
//
//  "Type/Trigger filtering" >> {
//    val hsAsAvalanche = avalancheForTest.copy(viewable = true, classification =
//      genClassification.sample.get.copy(avyType = AvalancheType.HS, trigger = AvalancheTrigger.AS))
//    val wsNeAvalanche = avalancheForTest.copy(viewable = true, classification =
//      genClassification.sample.get.copy(avyType = AvalancheType.WS, trigger = AvalancheTrigger.NE))
//
//    "Type filtering" >> {
//      insertAvalanches(hsAsAvalanche, wsNeAvalanche)
//      val wsTypeQuery = AvalancheQuery(avyType = Some(AvalancheType.WS))
//      verifySingleResult(dal, wsTypeQuery, wsNeAvalanche.extId)
//    }
//
//    "Trigger filtering" >> {
//      insertAvalanches(hsAsAvalanche, wsNeAvalanche)
//      val asTriggerQuery = AvalancheQuery(trigger = Some(AvalancheTrigger.AS))
//      verifySingleResult(dal, asTriggerQuery, hsAsAvalanche.extId)
//    }
//
//    "Type and trigger filtering" >> {
//      insertAvalanches(hsAsAvalanche, wsNeAvalanche)
//      val hsAsQuery = AvalancheQuery(avyType = Some(AvalancheType.HS), trigger = Some(AvalancheTrigger.AS))
//      verifySingleResult(dal, hsAsQuery, hsAsAvalanche.extId)
//    }
//  }
//
//  "R/D size filtering" >> {
//    val r4d15Avalanche = avalancheForTest.copy(viewable = true, classification =
//      genClassification.sample.get.copy(rSize = 4.0, dSize = 1.5))
//    val r15d3Avalanche = avalancheForTest.copy(viewable = true, classification =
//      genClassification.sample.get.copy(rSize = 1.5, dSize = 3.0))
//
//    "R size filtering" >> {
//      insertAvalanches(r4d15Avalanche, r15d3Avalanche)
//      val r4Query = AvalancheQuery(rSize = Some(4.0))
//      verifySingleResult(dal, r4Query, r4d15Avalanche.extId)
//    }
//
//    "D size filtering" >> {
//      insertAvalanches(r4d15Avalanche, r15d3Avalanche)
//      val d25Query = AvalancheQuery(dSize = Some(2.5))
//      verifySingleResult(dal, d25Query, r15d3Avalanche.extId)
//    }
//
//    "R and D size filtering" >> {
//      insertAvalanches(r4d15Avalanche, r15d3Avalanche)
//      val r3d1Query = AvalancheQuery(rSize = Some(3.0), dSize = Some(1.0))
//      verifySingleResult(dal, r3d1Query, r4d15Avalanche.extId)
//    }
//  }
//
//  "Human numbers filtering" >> {
//    val c4k0Avalanche = avalancheForTest.copy(viewable = true,
//      humanNumbers = genHumanNumbers.sample.get.copy(caught = 4, killed = 0))
//    val c3k2Avalanche = avalancheForTest.copy(viewable = true,
//      humanNumbers = genHumanNumbers.sample.get.copy(caught = 3, killed = 2))
//
//    "Number caught filtering" >> {
//      insertAvalanches(c4k0Avalanche, c3k2Avalanche)
//      val c4Query = AvalancheQuery(numCaught = Some(4))
//      verifySingleResult(dal, c4Query, c4k0Avalanche.extId)
//    }
//
//    "Number killed filtering" >> {
//      insertAvalanches(c4k0Avalanche, c3k2Avalanche)
//      val k1Query = AvalancheQuery(numKilled = Some(1))
//      verifySingleResult(dal, k1Query, c3k2Avalanche.extId)
//    }
//
//    "Number caught and killed filtering" >> {
//      insertAvalanches(c4k0Avalanche, c3k2Avalanche)
//      val c2k2Query = AvalancheQuery(numCaught = Some(2), numKilled = Some(2))
//      verifySingleResult(dal, c2k2Query, c3k2Avalanche.extId)
//    }
//  }
//
//  "Avalanche count" >> {
//    "Counts avalanches by viewability" >> {
//      val viewableAvalanche1 = avalancheForTest.copy(viewable = true)
//      val viewableAvalanche2 = avalancheForTest.copy(viewable = true)
//      val unviewableAvalanche = avalancheForTest.copy(viewable = false)
//
//      insertAvalanches(viewableAvalanche1, viewableAvalanche2, unviewableAvalanche)
//
//      dal.countAvalanches(Some(true)) mustEqual 2
//      dal.countAvalanches(Some(false)) mustEqual 1
//      dal.countAvalanches(None) mustEqual 3
//    }
//  }
//
//  "Ordering" >> {
//    val now = DateTime.now
//    val latest = avalancheForTest.copy(viewable = false, date = now)
//    val earliest = avalancheForTest.copy(viewable = false, date = now.minusDays(20))
//    val middle = avalancheForTest.copy(viewable = false, date = now.minusDays(10))
//
//    "Selects can be ordered by date ascending" >> {
//      insertAvalanches(latest, earliest, middle)
//
//      val dateAscOrderQuery = AvalancheQuery(order = List((OrderField.Date, OrderDirection.asc)))
//      val avyDateAscArray = dal.getAvalanches(dateAscOrderQuery).toArray
//
//      avyDateAscArray(0).extId mustEqual earliest.extId
//      avyDateAscArray(1).extId mustEqual middle.extId
//      avyDateAscArray(2).extId mustEqual latest.extId
//    }
//
//    "Selects can be ordered by date descending" >> {
//      insertAvalanches(latest, earliest, middle)
//
//      val dateDescOrderQuery = AvalancheQuery(order = List((OrderField.Date, OrderDirection.desc)))
//      val avyDateDescArray = dal.getAvalanches(dateDescOrderQuery).toArray
//
//      avyDateDescArray(0).extId mustEqual latest.extId
//      avyDateDescArray(1).extId mustEqual middle.extId
//      avyDateDescArray(2).extId mustEqual earliest.extId
//    }
//  }
//
//  "Pagination" >> {
//    "Selects can be paginated" >> {
//      insertAvalanches(avalancheForTest, avalancheForTest, avalancheForTest)
//
//      val firstPageQuery = AvalancheQuery(offset = 0, limit = 2)
//      val secondPageQuery = AvalancheQuery(offset = 2, limit = 2)
//
//      dal.getAvalanches(firstPageQuery).size mustEqual 2
//      dal.getAvalanches(secondPageQuery).size mustEqual 1
//    }
//  }
//
//  private def verifySingleResult(dao: CachedDAL, query: AvalancheQuery, extId: String): Result = {
//    val resultList = dao.getAvalanches(query)
//    resultList must haveLength(1)
//    resultList.head.extId mustEqual extId
//  }
}
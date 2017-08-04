package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.model.enums.{AvalancheInterface, AvalancheTrigger, AvalancheType}
import com.jamiesampey.avyeyes.service.ExternalIdService
import com.jamiesampey.avyeyes.util.Converters.strToDate
import org.joda.time.DateTime
import org.specs2.execute.Result
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.WithApplication


class AvalancheDaoSearchSelectTest extends DatabaseTest {

  implicit val subject = injector.instanceOf[AvalancheDao]

  "Date filtering" should {
    val jan1Avalanche = genAvalanche.generate.copy(viewable = true, date = strToDate("01-01-2014"))
    val jan5Avalanche = genAvalanche.generate.copy(viewable = true, date = strToDate("01-05-2014"))

    "from date filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(jan1Avalanche, jan5Avalanche)
      val fromDateQuery = AvalancheSpatialQuery(fromDate = Some(strToDate("01-03-2014")))
      verifySingleResult(fromDateQuery, jan5Avalanche.extId)
    }

    "to date filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(jan1Avalanche, jan5Avalanche)
      val toDateQuery = AvalancheSpatialQuery(toDate = Some(strToDate("01-03-2014")))
      verifySingleResult(toDateQuery, jan1Avalanche.extId)
    }

    "date filtering spanning year boundary" in new WithApplication(appBuilder.build) {
      insertAvalanches(jan1Avalanche, jan5Avalanche)
      val dateQuery = AvalancheSpatialQuery(fromDate = Some(strToDate("12-25-2013")), toDate = Some(strToDate("01-04-2014")))
      verifySingleResult(dateQuery, jan1Avalanche.extId)
    }
  }

  "Type/Trigger/Interface filtering" should {
    val hsAsAvalanche = genAvalanche.generate.copy(viewable = true, classification =
      genClassification.sample.get.copy(avyType = AvalancheType.HS, trigger = AvalancheTrigger.AS, interface = AvalancheInterface.O))
    val wsNeAvalanche = genAvalanche.generate.copy(viewable = true, classification =
      genClassification.sample.get.copy(avyType = AvalancheType.WS, trigger = AvalancheTrigger.NE, interface = AvalancheInterface.G))

    "type filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(hsAsAvalanche, wsNeAvalanche)
      val wsTypeQuery = AvalancheSpatialQuery(avyType = Some(AvalancheType.WS))
      verifySingleResult(wsTypeQuery, wsNeAvalanche.extId)
    }

    "trigger filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(hsAsAvalanche, wsNeAvalanche)
      val asTriggerQuery = AvalancheSpatialQuery(trigger = Some(AvalancheTrigger.AS))
      verifySingleResult(asTriggerQuery, hsAsAvalanche.extId)
    }

    "interface filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(hsAsAvalanche, wsNeAvalanche)
      val gInterfaceQuery = AvalancheSpatialQuery(interface = Some(AvalancheInterface.G))
      verifySingleResult(gInterfaceQuery, wsNeAvalanche.extId)
    }

    "type, trigger, and interface filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(hsAsAvalanche, wsNeAvalanche)
      val hsAsOQuery = AvalancheSpatialQuery(avyType = Some(AvalancheType.HS), trigger = Some(AvalancheTrigger.AS), interface = Some(AvalancheInterface.O))
      verifySingleResult(hsAsOQuery, hsAsAvalanche.extId)
    }
  }

  "R/D size filtering" should {
    val r4d15Avalanche = genAvalanche.generate.copy(viewable = true, classification =
      genClassification.sample.get.copy(rSize = 4.0, dSize = 1.5))
    val r15d3Avalanche = genAvalanche.generate.copy(viewable = true, classification =
      genClassification.sample.get.copy(rSize = 1.5, dSize = 3.0))

    "R size filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(r4d15Avalanche, r15d3Avalanche)
      val r4Query = AvalancheSpatialQuery(rSize = Some(4.0))
      verifySingleResult(r4Query, r4d15Avalanche.extId)
    }

    "D size filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(r4d15Avalanche, r15d3Avalanche)
      val d25Query = AvalancheSpatialQuery(dSize = Some(2.5))
      verifySingleResult(d25Query, r15d3Avalanche.extId)
    }

    "R and D size filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(r4d15Avalanche, r15d3Avalanche)
      val r3d1Query = AvalancheSpatialQuery(rSize = Some(3.0), dSize = Some(1.0))
      verifySingleResult(r3d1Query, r4d15Avalanche.extId)
    }
  }

  "Human numbers filtering" should {
    val c4k0Avalanche = genAvalanche.generate.copy(viewable = true,
      humanNumbers = genHumanNumbers.sample.get.copy(caught = 4, killed = 0))
    val c3k2Avalanche = genAvalanche.generate.copy(viewable = true,
      humanNumbers = genHumanNumbers.sample.get.copy(caught = 3, killed = 2))

    "number caught filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(c4k0Avalanche, c3k2Avalanche)
      val c4Query = AvalancheSpatialQuery(numCaught = Some(4))
      verifySingleResult(c4Query, c4k0Avalanche.extId)
    }

    "number killed filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(c4k0Avalanche, c3k2Avalanche)
      val k1Query = AvalancheSpatialQuery(numKilled = Some(1))
      verifySingleResult(k1Query, c3k2Avalanche.extId)
    }

    "number caught and killed filtering" in new WithApplication(appBuilder.build) {
      insertAvalanches(c4k0Avalanche, c3k2Avalanche)
      val c2k2Query = AvalancheSpatialQuery(numCaught = Some(2), numKilled = Some(2))
      verifySingleResult(c2k2Query, c3k2Avalanche.extId)
    }
  }

  "Avalanche count" should {
    "count avalanches by viewability" in new WithApplication(appBuilder.build) {
      val viewableAvalanche1 = genAvalanche.generate.copy(viewable = true)
      val viewableAvalanche2 = genAvalanche.generate.copy(viewable = true)
      val unviewableAvalanche = genAvalanche.generate.copy(viewable = false)

      insertAvalanches(viewableAvalanche1, viewableAvalanche2, unviewableAvalanche)

      subject.countAvalanches(Some(true)) mustEqual 2
      subject.countAvalanches(Some(false)) mustEqual 1
      subject.countAvalanches(None) mustEqual 3
    }
  }

  "Ordering" should {
    val now = DateTime.now
    val latest = genAvalanche.generate.copy(viewable = false, date = now)
    val earliest = genAvalanche.generate.copy(viewable = false, date = now.minusDays(20))
    val middle = genAvalanche.generate.copy(viewable = false, date = now.minusDays(10))

    "selects can be ordered by date ascending" in new WithApplication(appBuilder.build) {
      insertAvalanches(latest, earliest, middle)

      val dateAscOrderQuery = AvalancheSpatialQuery(order = List((OrderField.Date, OrderDirection.asc)))
      val avyDateAscArray = subject.getAvalanches(dateAscOrderQuery).toArray

      avyDateAscArray(0).extId mustEqual earliest.extId
      avyDateAscArray(1).extId mustEqual middle.extId
      avyDateAscArray(2).extId mustEqual latest.extId
    }

    "selects can be ordered by date descending" in new WithApplication(appBuilder.build) {
      insertAvalanches(latest, earliest, middle)

      val dateDescOrderQuery = AvalancheSpatialQuery(order = List((OrderField.Date, OrderDirection.desc)))
      val avyDateDescArray = subject.getAvalanches(dateDescOrderQuery).toArray

      avyDateDescArray(0).extId mustEqual latest.extId
      avyDateDescArray(1).extId mustEqual middle.extId
      avyDateDescArray(2).extId mustEqual earliest.extId
    }
  }

  "Pagination" should {
    "selects can be paginated" in new WithApplication(appBuilder.build) {
      insertAvalanches(genAvalanche.generate, genAvalanche.generate, genAvalanche.generate)

      val firstPageQuery = AvalancheSpatialQuery(offset = 0, limit = 2)
      val secondPageQuery = AvalancheSpatialQuery(offset = 2, limit = 2)

      subject.getAvalanches(firstPageQuery).size mustEqual 2
      subject.getAvalanches(secondPageQuery).size mustEqual 1
    }
  }

  private def verifySingleResult(query: AvalancheSpatialQuery, extId: String): Result = {
    val resultList = subject.getAvalanches(query)
    resultList must haveLength(1)
    resultList.head.extId mustEqual extId
  }
}

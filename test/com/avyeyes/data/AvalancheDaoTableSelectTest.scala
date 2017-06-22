package com.avyeyes.data

import play.api.test.WithApplication

class AvalancheDaoTableSelectTest extends DatabaseTest {

  implicit val subject = injector.instanceOf[AvalancheDao]

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val commonGeoBounds = GeoBounds(
    lngMax = commonLng + .01,
    lngMin = commonLng - .01,
    latMax = commonLat + .01,
    latMin = commonLat - .01
  )

  "Admin avalanche select filtering" should {
    val a1 = genAvalanche.generate.copy(extId = "94jfi449", viewable = false, areaName = "JoNeS Bowl")
    val a2 = genAvalanche.generate.copy(extId = "95fsov7p", viewable = false, areaName = "Highland Bowl")
    val a3 = genAvalanche.generate.copy(extId = "3wksovtq", viewable = false, areaName = "jones pass")

    "Filters by external ID" in new WithApplication(appBuilder.build) {
      insertAvalanches(a1, a2, a3)

      val query1 = AvalancheTableQuery(extId = Some("94j"))
      val result1 = subject.getAvalanchesAdmin(query1)
      val query2 = AvalancheTableQuery(extId = Some("sov"))
      val result2 = subject.getAvalanchesAdmin(query2)

      result1._1.head.extId mustEqual a1.extId
      result1._2 mustEqual 1
      result1._3 mustEqual 3
      result2._1.size mustEqual 2
      result2._2 mustEqual 2
      result2._3 mustEqual 3
    }

    "Filters by area name" in new WithApplication(appBuilder.build) {
      insertAvalanches(a1, a2, a3)

      val query1 = AvalancheTableQuery(areaName = Some("land"))
      val result1 = subject.getAvalanchesAdmin(query1)
      val query2 = AvalancheTableQuery(areaName = Some("jones"))
      val result2 = subject.getAvalanchesAdmin(query2)

      result1._1.head.extId mustEqual a2.extId
      result1._2 mustEqual 1
      result1._3 mustEqual 3
      result2._1.size mustEqual 2
      result2._2 mustEqual 2
      result2._3 mustEqual 3
    }

    "Filters by submitter email" in new WithApplication(appBuilder.build) {
      insertAvalanches(a1.copy(submitterEmail = "joe.brown@gmail.com"), a2.copy(submitterEmail = "neo@yahoo.com"), a3.copy(submitterEmail = "charlie_brownja@here.org"))

      val query1 = AvalancheTableQuery(submitterEmail = Some("org"))
      val result1 = subject.getAvalanchesAdmin(query1)
      val query2 = AvalancheTableQuery(submitterEmail = Some("BROWN"))
      val result2 = subject.getAvalanchesAdmin(query2)

      result1._1.head.extId mustEqual a3.extId
      result1._2 mustEqual 1
      result1._3 mustEqual 3
      result2._1.size mustEqual 2
      result2._2 mustEqual 2
      result2._3 mustEqual 3
    }
  }
}

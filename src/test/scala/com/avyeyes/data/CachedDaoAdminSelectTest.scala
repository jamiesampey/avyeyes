package com.avyeyes.data

import com.avyeyes.test.Generators._
import com.avyeyes.util.UnauthorizedException
import org.specs2.mutable.Specification

class CachedDaoAdminSelectTest extends Specification with InMemoryDB {
  sequential

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val commonGeoBounds = GeoBounds(
    lngMax = commonLng + .01,
    lngMin = commonLng - .01,
    latMax = commonLat + .01,
    latMin = commonLat - .01
  )
  
  "Admin avalanche select auth check" should {
    "Admin select not allowed with unauthorized session" >> {
      val nonviewableAvalanche = avalancheForTest.copy(viewable = false)
      mockUserSession.isAuthorizedSession() returns false
      dal.insertAvalanche(nonviewableAvalanche)
      dal.getAvalanchesAdmin(AdminAvalancheQuery()) must throwA[UnauthorizedException]
    }

    "Admin select allowed with authorized session" >> {
      val nonviewableAvalanche = avalancheForTest.copy(viewable = false)
      mockUserSession.isAuthorizedSession() returns true
      dal.insertAvalanche(nonviewableAvalanche)
      dal.getAvalanchesAdmin(AdminAvalancheQuery())._1.size must_== 1
      dal.getAvalanchesAdmin(AdminAvalancheQuery())._2 must_== 1
      dal.getAvalanchesAdmin(AdminAvalancheQuery())._3 must_== 1
    }
  }

  "Admin avalanche select filtering" should {
    mockUserSession.isAuthorizedSession() returns true

    val a1 = avalancheForTest.copy(extId = "94jfi449", viewable = false, areaName = "JoNeS Bowl")
    val a2 = avalancheForTest.copy(extId = "95fsov7p", viewable = false, areaName = "Highland Bowl")
    val a3 = avalancheForTest.copy(extId = "3wksovtq", viewable = false, areaName = "jones pass")

    "Filters by external ID" >> {
      dal.insertAvalanche(a1)
      dal.insertAvalanche(a2)
      dal.insertAvalanche(a3)

      val query1 = AdminAvalancheQuery(extId = Some("%94j%"))
      val result1 = dal.getAvalanchesAdmin(query1)
      val query2 = AdminAvalancheQuery(extId = Some("%sov%"))
      val result2 = dal.getAvalanchesAdmin(query2)

      result1._1(0).extId must_== a1.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }

    "Filters by area name" >> {
      dal.insertAvalanche(a1)
      dal.insertAvalanche(a2)
      dal.insertAvalanche(a3)

      val query1 = AdminAvalancheQuery(areaName = Some("%land%"))
      val result1 = dal.getAvalanchesAdmin(query1)
      val query2 = AdminAvalancheQuery(areaName = Some("%jones%"))
      val result2 = dal.getAvalanchesAdmin(query2)

      result1._1(0).extId must_== a2.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }

    "Filters by submitter email" >> {
      dal.insertAvalanche(a1.copy(submitterEmail = "joe.brown@gmail.com"))
      dal.insertAvalanche(a2.copy(submitterEmail = "neo@yahoo.com"))
      dal.insertAvalanche(a3.copy(submitterEmail = "charlie_brownja@here.org"))

      val query1 = AdminAvalancheQuery(submitterEmail = Some("%org%"))
      val result1 = dal.getAvalanchesAdmin(query1)
      val query2 = AdminAvalancheQuery(submitterEmail = Some("%BROWN%"))
      val result2 = dal.getAvalanchesAdmin(query2)

      result1._1(0).extId must_== a3.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }
  }
}
package com.avyeyes.data

import com.avyeyes.test.Generators._
import com.avyeyes.util.UnauthorizedException
import org.specs2.mutable.Specification

class CachedDaoAdminSelectTest extends Specification with InMemoryDB {
  sequential

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val commonGeoBounds = GeoBounds(commonLat+.01, commonLng+.01, commonLat-.01, commonLng-.01)
  
  "Admin avalanche select auth check" should {
    "Admin select not allowed with unauthorized session" >> {
      val nonviewableAvalanche = avalancheForTest.copy(viewable = false)
      val dao = new MemoryMapCachedDao(NotAuthorized)
      dao.insertAvalanche(nonviewableAvalanche)
      dao.getAvalanchesAdmin(AdminAvalancheQuery()) must throwA[UnauthorizedException]
    }

    "Admin select allowed with authorized session" >> {
      val nonviewableAvalanche = avalancheForTest.copy(viewable = false)
      val dao = new MemoryMapCachedDao(Authorized)
      dao.insertAvalanche(nonviewableAvalanche)
      dao.getAvalanchesAdmin(AdminAvalancheQuery())._1.size must_== 1
      dao.getAvalanchesAdmin(AdminAvalancheQuery())._2 must_== 1
      dao.getAvalanchesAdmin(AdminAvalancheQuery())._3 must_== 1
    }
  }

  "Admin avalanche select filtering" should {
    val dao = new MemoryMapCachedDao(Authorized)

    val a1 = avalancheForTest.copy(extId = "94jfi449", viewable = false, areaName = "JoNeS Bowl")
    val a2 = avalancheForTest.copy(extId = "95fsov7p", viewable = false, areaName = "Highland Bowl")
    val a3 = avalancheForTest.copy(extId = "3wksovtq", viewable = false, areaName = "jones pass")

    "Filters by external ID" >> {
      dao.insertAvalanche(a1)
      dao.insertAvalanche(a2)
      dao.insertAvalanche(a3)

      val query1 = AdminAvalancheQuery(extId = Some("%94j%"))
      val result1 = dao.getAvalanchesAdmin(query1)
      val query2 = AdminAvalancheQuery(extId = Some("%sov%"))
      val result2 = dao.getAvalanchesAdmin(query2)

      result1._1(0).extId must_== a1.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }

    "Filters by area name" >> {
      dao.insertAvalanche(a1)
      dao.insertAvalanche(a2)
      dao.insertAvalanche(a3)

      val query1 = AdminAvalancheQuery(areaName = Some("%land%"))
      val result1 = dao.getAvalanchesAdmin(query1)
      val query2 = AdminAvalancheQuery(areaName = Some("%jones%"))
      val result2 = dao.getAvalanchesAdmin(query2)

      result1._1(0).extId must_== a2.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }

    "Filters by submitter email" >> {
      dao.insertAvalanche(a1.copy(submitterEmail = "joe.brown@gmail.com"))
      dao.insertAvalanche(a2.copy(submitterEmail = "neo@yahoo.com"))
      dao.insertAvalanche(a3.copy(submitterEmail = "charlie_brownja@here.org"))

      val query1 = AdminAvalancheQuery(submitterEmail = Some("%org%"))
      val result1 = dao.getAvalanchesAdmin(query1)
      val query2 = AdminAvalancheQuery(submitterEmail = Some("%BROWN%"))
      val result2 = dao.getAvalanchesAdmin(query2)

      result1._1(0).extId must_== a3.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }
  }
}
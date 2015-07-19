package com.avyeyes.data

import com.avyeyes.data.{GeoBounds, AdminAvalancheQuery}
import com.avyeyes.test.Generators
import com.avyeyes.util.UnauthorizedException
import org.specs2.mutable.Specification

class CachedDaoAdminSelectTest extends Specification with InMemoryDB with Generators {
  sequential

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val commonGeoBounds = GeoBounds(commonLat+.01, commonLng+.01, commonLat-.01, commonLng-.01)
  
  "Admin avalanche select auth check" >> {
    "Admin select not allowed with unauthorized session" >> {
      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      val dao = new SquerylAvalancheDao(NotAuthorized)
      insertTestAvalanche(dao, nonviewableAvalanche)
      dao.selectAvalanchesForAdminTable(AdminAvalancheQuery()) must throwA[UnauthorizedException]
    }

    "Admin select allowed with authorized session" >> {
      val nonviewableAvalanche = avalancheAtLocation("94jfi449", false, commonLat, commonLng)
      val dao = new SquerylAvalancheDao(Authorized)
      insertTestAvalanche(dao, nonviewableAvalanche)
      dao.selectAvalanchesForAdminTable(AdminAvalancheQuery())._1.size must_== 1
      dao.selectAvalanchesForAdminTable(AdminAvalancheQuery())._2 must_== 1
      dao.selectAvalanchesForAdminTable(AdminAvalancheQuery())._3 must_== 1
    }
  }

  "Admin avalanche select filtering" >> {
    val dao = new SquerylAvalancheDao(Authorized)

    val a1 = avalancheWithNameAndSubmitter("94jfi449", false, commonLat, commonLng, "JoNeS Bowl", "")
    val a2 = avalancheWithNameAndSubmitter("95fsov7p", false, commonLat, commonLng, "Highland Bowl", "")
    val a3 = avalancheWithNameAndSubmitter("3wksovtq", false, commonLat, commonLng, "jones pass", "")

    "Filters by external ID" >> {
      insertTestAvalanche(dao, a1)
      insertTestAvalanche(dao, a2)
      insertTestAvalanche(dao, a3)

      val query1 = AdminAvalancheQuery(extId = Some("%94j%"))
      val result1 = dao.selectAvalanchesForAdminTable(query1)
      val query2 = AdminAvalancheQuery(extId = Some("%sov%"))
      val result2 = dao.selectAvalanchesForAdminTable(query2)

      result1._1(0).extId must_== a1.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }

    "Filters by area name" >> {
      insertTestAvalanche(dao, a1)
      insertTestAvalanche(dao, a2)
      insertTestAvalanche(dao, a3)

      val query1 = AdminAvalancheQuery(areaName = Some("%land%"))
      val result1 = dao.selectAvalanchesForAdminTable(query1)
      val query2 = AdminAvalancheQuery(areaName = Some("%jones%"))
      val result2 = dao.selectAvalanchesForAdminTable(query2)

      result1._1(0).extId must_== a2.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }

    "Filters by submitter email" >> {
      insertTestAvalanche(dao, a1, "joe.brown@gmail.com")
      insertTestAvalanche(dao, a2, "neo@yahoo.com")
      insertTestAvalanche(dao, a3, "charlie_brownja@here.org")

      val query1 = AdminAvalancheQuery(submitterEmail = Some("%org%"))
      val result1 = dao.selectAvalanchesForAdminTable(query1)
      val query2 = AdminAvalancheQuery(submitterEmail = Some("%BROWN%"))
      val result2 = dao.selectAvalanchesForAdminTable(query2)

      result1._1(0).extId must_== a3.extId
      result1._2 must_== 1
      result1._3 must_== 3
      result2._1.size must_== 2
      result2._2 must_== 2
      result2._3 must_== 3
    }
  }
}
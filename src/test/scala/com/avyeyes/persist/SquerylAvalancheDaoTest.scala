package com.avyeyes.persist

import org.specs2.mutable.Specification

import com.avyeyes.test.AvalancheGenerator

class SquerylAvalancheDaoTest extends Specification with InMemoryDB with AvalancheGenerator {
 
  "Avalanche insertion" should {
    "work" in {
      val a1 = avalancheAtLocation("4a3jr23k", false, 39.59349550, -103.59349050)
      val dao = new SquerylAvalancheDao
      dao insertAvalanche a1
      val readAvalanche = dao.selectAvalanche(a1.extId).get
      
      readAvalanche.extId must_== a1.extId
      readAvalanche.lat must_== a1.lat
      readAvalanche.lng must_== a1.lng
    }
  }
}
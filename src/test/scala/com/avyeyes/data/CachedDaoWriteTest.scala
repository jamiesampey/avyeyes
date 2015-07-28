package com.avyeyes.data

import com.avyeyes.test.Generators._
import com.avyeyes.util.UnauthorizedException
import org.specs2.mutable.Specification

class CachedDaoWriteTest extends Specification with InMemoryDB {
  sequential

  "Avalanche insert" >> {
    "Inserts an avalanche" >> {
      val dao = new MemoryMapCachedDao(Authorized)

      val al = avalancheForTest.copy(viewable = true)
      dao.insertAvalanche(a1)
      val selectResult = dao.getAvalanche(a1.extId).get
      
      selectResult.extId mustEqual a1.extId
    }
    
    "Unauthorized session cannot insert a viewable avalanche" >> {
      val dao = new MemoryMapCachedDao(NotAuthorized)

      dao.insertAvalanche(avalancheForTest.copy(viewable = true)) must throwA[UnauthorizedException]
    }
  }
    
  "Avalanche update" >> {
    "Not allowed with unauthorized session" >> {
      val dao = new MemoryMapCachedDao(NotAuthorized)

      val origAvalanche = avalancheForTest.copy(viewable = false)
      dao.insertAvalanche(origAvalanche)
      val updatedAvalanche = origAvalanche.copy(viewable = true)
      
      dao.updateAvalanche(updatedAvalanche) must throwA[UnauthorizedException]
    }
    
    "Allowed with authorized session" >> {
      val dao = new MemoryMapCachedDao(Authorized)

      val origAvalanche = avalancheForTest.copy(viewable = false)
      dao.insertAvalanche(origAvalanche)
      val updatedAvalanche = origAvalanche.copy(viewable = true)
      dao.updateAvalanche(updatedAvalanche)
      
      val selectResult = dao.getAvalanche(origAvalanche.extId).get
      selectResult.viewable mustEqual true
    }
    
    "Modifies all updatable avalanche fields" >> {
      val dao = new MemoryMapCachedDao(Authorized)

      val origAvalanche = avalancheForTest
      dao.insertAvalanche(origAvalanche)

      val updatedAvalanche = avalancheForTest.copy(extId = origAvalanche.extId)
      dao.updateAvalanche(updatedAvalanche)
      
      val result = dao.getAvalanche(origAvalanche.extId).get
      result.createTime mustEqual origAvalanche.createTime
      result.viewable mustEqual updatedAvalanche.viewable
      result.submitterExp mustEqual updatedAvalanche.submitterExp
      result.submitterEmail mustEqual updatedAvalanche.submitterEmail
      result.location mustEqual origAvalanche.location
      result.areaName mustEqual updatedAvalanche.areaName
      result.date mustEqual updatedAvalanche.date
      result.scene mustEqual updatedAvalanche.scene
      result.slope mustEqual updatedAvalanche.slope
      result.classification mustEqual updatedAvalanche.classification
      result.humanNumbers mustEqual updatedAvalanche.humanNumbers
      result.comments mustEqual updatedAvalanche.comments
      result.perimeter mustEqual origAvalanche.perimeter
    }
  }
  
  "Avalanche delete" >> {
    "Not allowed with unauthorized session" >> {
      val dao = new MemoryMapCachedDao(NotAuthorized)

      val a1 = avalancheForTest
      dao.insertAvalanche(avalancheForTest)

      dao.deleteAvalanche(avalancheForTest.extId) must throwA[UnauthorizedException]
    }
    
    "Allowed (and works) with authorized session" >> {
      val dao = new MemoryMapCachedDao(Authorized)

      val a1 = avalancheForTest
      val a2 = avalancheForTest

      dao.insertAvalanche(a1)
      dao.insertAvalanche(a2)
      dao.getAvalanche(a1.extId) must beSome
      dao.getAvalanche(a2.extId) must beSome
      
      dao.deleteAvalanche(a1.extId)
      
      dao.getAvalanche(a1.extId) must beNone
      dao.getAvalanche(a2.extId) must beSome
      
      dao.deleteAvalanche(a2.extId)
      
      dao.getAvalanche(a2.extId) must beNone
    }
  }
}
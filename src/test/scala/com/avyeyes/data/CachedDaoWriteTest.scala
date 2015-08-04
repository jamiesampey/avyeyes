package com.avyeyes.data

import com.avyeyes.test.Generators._
import com.avyeyes.util.UnauthorizedException
import org.specs2.mutable.Specification

class CachedDaoWriteTest extends Specification with InMemoryDB {
  sequential

  "Avalanche insert" >> {
    "Inserts an avalanche" >> {
      mockUserSession.isAuthorizedSession() returns true

      val a1 = avalancheForTest.copy(viewable = true)
      dal.insertAvalanche(a1)

      dal.getAvalanche(a1.extId).get mustEqual a1
      dal.getAvalancheFromDisk(a1.extId).get mustEqual a1
    }
    
    "Unauthorized session cannot insert a viewable avalanche" >> {
      mockUserSession.isAuthorizedSession() returns false
      dal.insertAvalanche(avalancheForTest.copy(viewable = true)) must throwA[UnauthorizedException]
    }
  }
    
  "Avalanche update" >> {
    "Not allowed with unauthorized session" >> {
      mockUserSession.isAuthorizedSession() returns false

      val origAvalanche = avalancheForTest.copy(viewable = false)
      dal.insertAvalanche(origAvalanche)
      val updatedAvalanche = origAvalanche.copy(viewable = true)
      
      dal.updateAvalanche(updatedAvalanche) must throwA[UnauthorizedException]
    }
    
    "Allowed with authorized session" >> {
      mockUserSession.isAuthorizedSession() returns true

      val origAvalanche = avalancheForTest.copy(viewable = false)
      dal.insertAvalanche(origAvalanche)
      val updatedAvalanche = origAvalanche.copy(viewable = true)
      dal.updateAvalanche(updatedAvalanche)
      
      val selectResult = dal.getAvalanche(origAvalanche.extId).get
      selectResult.viewable mustEqual true
    }
    
    "Modifies all updatable avalanche fields" >> {
      mockUserSession.isAuthorizedSession() returns true

      val origAvalanche = avalancheForTest
      dal.insertAvalanche(origAvalanche)

      val updatedAvalanche = avalancheForTest.copy(extId = origAvalanche.extId)
      dal.updateAvalanche(updatedAvalanche)
      
      val result = dal.getAvalanche(origAvalanche.extId).get
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
      mockUserSession.isAuthorizedSession() returns false

      val a1 = avalancheForTest.copy(viewable = false)
      dal.insertAvalanche(a1)

      dal.deleteAvalanche(a1.extId) must throwA[UnauthorizedException]
    }
    
    "Allowed (and works) with authorized session" >> {
      mockUserSession.isAuthorizedSession() returns true

      val a1 = avalancheForTest
      val a2 = avalancheForTest

      dal.insertAvalanche(a1)
      dal.insertAvalanche(a2)
      dal.getAvalanche(a1.extId) must beSome
      dal.getAvalanche(a2.extId) must beSome
      
      dal.deleteAvalanche(a1.extId)
      
      dal.getAvalanche(a1.extId) must beNone
      dal.getAvalanche(a2.extId) must beSome
      
      dal.deleteAvalanche(a2.extId)
      
      dal.getAvalanche(a2.extId) must beNone
    }
  }
}
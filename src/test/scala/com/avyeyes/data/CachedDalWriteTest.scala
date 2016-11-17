package com.avyeyes.data

import com.avyeyes.service.UnauthorizedException
import com.avyeyes.util.Constants._
import com.avyeyes.util.FutureOps._
import com.avyeyes.test.Generators._
import org.joda.time.DateTime
import org.specs2.mutable.Specification

class CachedDalWriteTest extends Specification with InMemoryDB {
  sequential

  val moreThanEditWindow = AvalancheEditWindow.toMillis + 1000

  "Avalanche insert" >> {
    "Admin can insert at any time" >> {
      mockUserSession.isAdminSession returns true
      val a1 = avalancheForTest.copy(createTime = DateTime.now.minus(moreThanEditWindow))
      dal.insertAvalanche(a1)

      dal.getAvalanche(a1.extId).get mustEqual a1.copy(comments = None)
      dal.getAvalancheFromDisk(a1.extId).resolve.get mustEqual a1
    }

    "Submitter can insert a newly reserved ext id" >> {
      mockUserSession.isAdminSession returns false
      val a1 = avalancheForTest.copy(extId = dal.reserveNewExtId(dal), viewable = true)
      dal.insertAvalanche(a1)

      dal.getAvalanche(a1.extId).get mustEqual a1.copy(comments = None)
      dal.getAvalancheFromDisk(a1.extId).resolve.get mustEqual a1
    }

    "Otherwise user cannot insert" >> {
      mockUserSession.isAdminSession returns false
      val a2 = avalancheForTest.copy(createTime = DateTime.now.minus(moreThanEditWindow))
      dal.insertAvalanche(a2) must throwA[UnauthorizedException]
    }
  }
    
  "Avalanche update" >> {
    "Admin can update at any time" >> {
      mockUserSession.isAdminSession returns true
      val origAvalanche = avalancheForTest.copy(viewable = false, createTime = DateTime.now.minus(moreThanEditWindow))
      dal.insertAvalanche(origAvalanche)

      val updatedAvalanche = origAvalanche.copy(viewable = true)
      dal.updateAvalanche(updatedAvalanche)
      val selectResult = dal.getAvalanche(origAvalanche.extId).get
      selectResult.viewable mustEqual true
    }

    "Submitter can update within the edit window" >> {
      mockUserSession.isAdminSession returns true
      val origAvalanche = avalancheForTest.copy(viewable = true, createTime = DateTime.now, areaName = "Joes Trees")
      dal.insertAvalanche(origAvalanche)

      mockUserSession.isAdminSession returns false
      val updatedAvalanche = origAvalanche.copy(areaName = "Mikes Trees")
      dal.updateAvalanche(updatedAvalanche)
      dal.getAvalanche(origAvalanche.extId).get.areaName mustEqual updatedAvalanche.areaName
    }
    
    "Otherwise user cannot update" >> {
      mockUserSession.isAdminSession returns true
      val origAvalanche = avalancheForTest.copy(createTime = DateTime.now.minus(moreThanEditWindow))
      dal.insertAvalanche(origAvalanche)

      mockUserSession.isAdminSession returns false
      val updatedAvalanche = origAvalanche.copy(areaName = "Joes Trees")
      dal.updateAvalanche(updatedAvalanche) must throwA[UnauthorizedException]
    }

    "Update modifies all updatable avalanche fields" >> {
      mockUserSession.isAdminSession returns true

      val origAvalanche = avalancheForTest
      dal.insertAvalanche(origAvalanche)

      val updatedAvalanche = avalancheForTest.copy(extId = origAvalanche.extId)
      dal.updateAvalanche(updatedAvalanche)

      val result = dal.getAvalancheFromDisk(origAvalanche.extId).resolve.get
      result.createTime mustEqual origAvalanche.createTime
      result.viewable mustEqual updatedAvalanche.viewable
      result.submitterExp mustEqual updatedAvalanche.submitterExp
      result.submitterEmail mustEqual updatedAvalanche.submitterEmail
      result.location mustEqual origAvalanche.location
      result.areaName mustEqual updatedAvalanche.areaName
      result.date mustEqual updatedAvalanche.date
      result.weather mustEqual updatedAvalanche.weather
      result.slope.aspect mustEqual updatedAvalanche.slope.aspect
      result.slope.angle mustEqual updatedAvalanche.slope.angle
      result.slope.elevation mustEqual origAvalanche.slope.elevation
      result.classification mustEqual updatedAvalanche.classification
      result.humanNumbers mustEqual updatedAvalanche.humanNumbers
      result.comments mustEqual updatedAvalanche.comments
      result.perimeter mustEqual origAvalanche.perimeter
    }
  }
  
  "Avalanche delete" >> {
    "Non-admin user cannot delete" >> {
      mockUserSession.isAdminSession returns true
      val a1 = avalancheForTest
      dal.insertAvalanche(a1)

      mockUserSession.isAdminSession returns false
      dal.deleteAvalanche(a1.extId) must throwA[UnauthorizedException]
    }
    
    "Admin can always delete (and delete works as expected)" >> {
      mockUserSession.isAdminSession returns true

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
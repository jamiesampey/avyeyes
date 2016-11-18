package com.avyeyes.data

import com.avyeyes.test.Generators._
import com.avyeyes.util.Constants._
import com.avyeyes.util.FutureOps._
import org.specs2.mutable.Specification

class CachedDalWriteTest extends Specification with InMemoryDB {
  sequential

  val moreThanEditWindow = AvalancheEditWindow.toMillis + 1000

  "Avalanche update" >> {
    "Updates all modifiable fields" >> {
      val origAvalanche = avalancheForTest
      insertAvalanches(origAvalanche)

      val updatedAvalanche = avalancheForTest.copy(extId = origAvalanche.extId)
      dal.updateAvalanche(updatedAvalanche).resolve

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
    "works as expected" >> {
      val a1 = avalancheForTest
      val a2 = avalancheForTest
      insertAvalanches(a1, a2)

      dal.getAvalanche(a1.extId) must beSome
      dal.getAvalanche(a2.extId) must beSome
      
      dal.deleteAvalanche(a1.extId).resolve
      
      dal.getAvalanche(a1.extId) must beNone
      dal.getAvalanche(a2.extId) must beSome
      
      dal.deleteAvalanche(a2.extId).resolve
      
      dal.getAvalanche(a2.extId) must beNone
    }
  }
}
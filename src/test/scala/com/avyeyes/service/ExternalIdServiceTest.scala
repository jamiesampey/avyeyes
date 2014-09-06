package com.avyeyes.service

import com.avyeyes.test._
import com.avyeyes.model._
import com.avyeyes.util.AEHelpers._

class ExternalIdServiceTest extends AvyEyesSpec {
  isolated
  
  class ExtIdTester extends ExternalIdService
  val extIdTester = new ExtIdTester
  
  "External ID reservation" should {
    "Reserve an external ID if one is available" withSFor("/") in {
      mockAvalancheDao.selectAvalanche(any[String]) returns None
      val newExtId = extIdTester.reserveNewExtId(mockAvalancheDao)
      
      isValidExtId(Some(newExtId)) must beTrue
      ExternalIdMaitreD.reservationExists(newExtId) must beTrue
    }
    
   "Give up (and throw a RuntimeException) after a specified number of tries" withSFor("/") in {
     mockAvalancheDao.selectAvalanche(any[String]) returns Some(new Avalanche)
     try {
       extIdTester.reserveNewExtId(mockAvalancheDao)
       failure
     } catch {
       case e: RuntimeException => success
     }
   }
  }
    
  "External ID unreservation" should {  
    "Unreserve an external ID if it exists in the reservation cache" withSFor("/") in {
      mockAvalancheDao.selectAvalanche(any[String]) returns None
      val newExtId = extIdTester.reserveNewExtId(mockAvalancheDao)
      extIdTester.unreserveExtId(newExtId)      
      
      ExternalIdMaitreD.reservationExists(newExtId) must beFalse
    }
    
    "Exit gracefully if asked to unreserve a non-existent external ID" withSFor("/") in {
      try {
        extIdTester.unreserveExtId("4ki4")
        success
      } catch {
        case e: Exception => failure
      }
    }
  }
}
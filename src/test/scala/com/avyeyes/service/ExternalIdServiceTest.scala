package com.avyeyes.service

import com.avyeyes.data.CachedDAL
import com.avyeyes.test.Generators._
import com.avyeyes.util.Validators.isValidExtId
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.util.{Success, Try, Failure}

class ExternalIdServiceTest extends Specification with Mockito {
  isolated

  class Setup extends Scope {
    implicit val dal = mock[CachedDAL]

    class ExtIdTester extends ExternalIdService
    val extIdTester = new ExtIdTester
  }

  "External ID reservation" >> {
    "Reserve an external ID if one is available" in new Setup {
      dal.getAvalanche(any[String]) returns None
      val newExtId = extIdTester.reserveNewExtId
      
      isValidExtId(newExtId) must beTrue
      extIdTester.reservationExists(newExtId) must beTrue
    }
    
   "Give up (and throw an Exception) after a specified number of tries" in new Setup {
     dal.getAvalanche(any[String]) returns Some(avalancheForTest)

     Try(extIdTester.reserveNewExtId) match {
       case Success(_) => failure
       case Failure(ex) => success
     }
   }
  }
    
  "External ID unreservation" >> {
    "Unreserve an external ID if it exists in the reservation cache" in new Setup {
      dal.getAvalanche(any[String]) returns None
      val newExtId = extIdTester.reserveNewExtId
      extIdTester.unreserveExtId(newExtId)

      extIdTester.reservationExists(newExtId) must beFalse
    }
    
    "Exit gracefully if asked to unreserve a non-existent external ID" in new Setup {
      Try(extIdTester.unreserveExtId("4ki4")) match {
        case Success(_) => success
        case Failure(ex) => failure
      }
    }
  }

  "Bad word check" >> {
    "Catch bad words in a string" in new Setup {
      extIdTester.containsBadWord("what a fucking day!") must beTrue
      extIdTester.containsBadWord("what a lovely day!") must beFalse
    }

    "Catch bad words in external IDs" in new Setup {
      extIdTester.containsBadWord("193tit3k") must beTrue
      extIdTester.containsBadWord("49fk9d3k") must beFalse
    }
  }
}
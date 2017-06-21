package com.avyeyes.service

import com.avyeyes.data.CachedDao
import helpers.BaseSpec
import org.specs2.specification.Scope
import play.api.Logger

import scala.util.{Failure, Success, Try}

class ExternalIdServiceTest extends BaseSpec {

  class Setup extends Scope {
    implicit protected val mockDao = mock[CachedDao]

    class ExtIdTester extends ExternalIdService(mock[Logger])
    val extIdTester = new ExtIdTester
  }

  "External ID reservation" >> {
    "Reserve an external ID if one is available" in new Setup {
      mockDao.getAvalanche(any[String]) returns None
      val newExtId = extIdTester.reserveNewExtId

      extIdTester.reservationExists(newExtId) must beTrue
    }

   "Give up (and throw an Exception) after a specified number of tries" in new Setup {
     mockDao.getAvalanche(any[String]) returns Some(genAvalanche.generate)

     Try(extIdTester.reserveNewExtId) match {
       case Success(_) => failure
       case Failure(ex) => success
     }
   }
  }

  "External ID unreservation" >> {
    "Unreserve an external ID if it exists in the reservation cache" in new Setup {
      mockDao.getAvalanche(any[String]) returns None
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
}
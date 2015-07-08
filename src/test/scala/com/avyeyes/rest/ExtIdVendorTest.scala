package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.test._
import com.avyeyes.util.Helpers._
import net.liftweb.http._

class ExtIdVendorTest extends WebSpec2 with MockInjectors with LiftHelpers {
  sequential

  val extIdVendor = new ExtIdVendor
  val noAvalanche: Option[Avalanche] = None
  
  "Valid external ID request" should {
    "Return a new external ID" withSFor("http://avyeyes.com/rest/reserveExtId") in {
      mockAvalancheDao.getAvalanche(any[String]) returns(noAvalanche)
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(extIdVendor(req)())
      val returnedExtId = extractJsonStringField(resp, "extId")
     
      resp must beAnInstanceOf[JsonResponse]
      isValidExtId(Some(returnedExtId)) must beTrue
    }
    
    "Return InternalServerErrorResponse (500) if an extId could not be reserved" withSFor("http://avyeyes.com/rest/reserveExtId") in {
      mockAvalancheDao.getAvalanche(any[String]) throws new RuntimeException("test RTE")
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(extIdVendor(req)())

      resp must beAnInstanceOf[InternalServerErrorResponse]
    }
  }
}
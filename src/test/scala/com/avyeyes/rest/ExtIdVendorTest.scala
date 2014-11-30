package com.avyeyes.rest

import com.avyeyes.test._
import com.avyeyes.model.Avalanche
import com.avyeyes.util.Helpers._

import net.liftweb.http._

import org.mockito.Mockito._

class ExtIdVendorTest extends WebSpec2 with MockPersistence with LiftHelpers {
  val noAvalanche: Option[Avalanche] = None
  
  // first avalanche lookup returns None, so the new reservation can be made.
  // second avalanche lookup (second test example) throws exception (resulting in a 500 response)
  when(mockAvalancheDao.selectAvalanche(any[String])).thenReturn(noAvalanche).thenThrow(new RuntimeException("test RTE"))
    
  "Valid external ID request" should {
    sequential 
    
    "Return a new external ID" withSFor("http://avyeyes.com/rest/reserveExtId") in {
     val req = openLiftReqBox(S.request)
     val resp = openLiftRespBox(ExtIdVendor(req)())
     val returnedExtId = extractJsonStringField(resp, "extId")
     
     resp must beAnInstanceOf[JsonResponse]
     isValidExtId(Some(returnedExtId)) must beTrue
    }
    
    "Return InternalServerErrorResponse (500) if an extId could not be reserved" withSFor("http://avyeyes.com/rest/reserveExtId") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(ExtIdVendor(req)())

      resp must beAnInstanceOf[InternalServerErrorResponse]
    }
  }
}
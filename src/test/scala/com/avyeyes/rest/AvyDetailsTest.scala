package com.avyeyes.rest

import org.specs2.mutable.Specification

import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.test._

import net.liftweb.http._
import net.liftweb.mockweb.MockWeb

class AvyDetailsTest extends Specification with MockPersistence with AvalancheGenerator with LiftHelpers {
  // Testing an OBJECT (singleton), so the mockAvalancheDao is inserted ONCE. 
  // Only one chance to mock all methods.

  val extId1 = "4jf93dkj"
  val a1 = avalancheAtLocation(extId1, true, 41.6634870900582, -103.875046142935)
  mockAvalancheDao.selectViewableAvalanche(extId1) returns Some(a1)  
  mockAvalancheDao.selectAvalancheImageFilenames(extId1) returns Nil
      
  val badExtId = "blah"
  val noAvalanche: Option[Avalanche] = None
  mockAvalancheDao.selectViewableAvalanche(badExtId) returns noAvalanche
      
  "Valid avalanche details REST request" should {
    "Return avalanche details" in {
      MockWeb.testS("http://avyeyes.com/rest/avydetails/" + extId1) {
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(AvyDetails(req)())
          
        resp must beAnInstanceOf[JsonResponse]
        extractJsonStringField(resp, "extId") must_== extId1
      }
    }
    
    "Return reader-friendly labels for enum code fields" in {
      MockWeb.testS("http://avyeyes.com/rest/avydetails/" + extId1) {
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(AvyDetails(req)())
        
        extractJsonStringField(resp, "submitterExp") must_== ExperienceLevel.getEnumLabel(a1.submitterExp)
        extractJsonStringField(resp, "sky") must_== Sky.getEnumLabel(a1.sky)
        extractJsonStringField(resp, "precip") must_== Precip.getEnumLabel(a1.precip)
        extractJsonStringField(resp, "aspect") must_== Aspect.getEnumLabel(a1.aspect)
        extractJsonStringField(resp, "avyType") must_== AvalancheType.getEnumLabel(a1.avyType)
        extractJsonStringField(resp, "trigger") must_== AvalancheTrigger.getEnumLabel(a1.trigger)
        extractJsonStringField(resp, "bedSurface") must_== AvalancheInterface.getEnumLabel(a1.bedSurface)      
        extractJsonStringField(resp, "modeOfTravel") must_== ModeOfTravel.getEnumLabel(a1.modeOfTravel)
      }
    }
  }
  
  "Invalid avalanche details REST request" should {
    "Return NotFoundResponse" in {
      MockWeb.testS("http://avyeyes.com/rest/avydetails/" + badExtId) {
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(AvyDetails(req)())
      
        resp must beAnInstanceOf[NotFoundResponse]
      }
    }
  }
}

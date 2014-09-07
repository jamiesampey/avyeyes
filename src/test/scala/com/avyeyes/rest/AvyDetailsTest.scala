package com.avyeyes.rest

import com.avyeyes.test._

import net.liftweb.http.S
import net.liftweb.mocks.MockHttpServletRequest

class AvyDetailsTest extends WebSpec2 with MockPersistence with AvalancheGenerator with LiftHelpers {
  "Avalanche details rest endpoint" should {
    val a1 = avalancheAtLocation("4jf93dkj", true, 41.6634870900582, -103.875046142935)
    val mockDetailsRequest = new MockHttpServletRequest("http://avyeyes.com/rest/avydetails/" + a1.extId)
      
    "Return avalanche details if avalanche exists" withSFor(mockDetailsRequest) in {
      mockAvalancheDao.selectViewableAvalanche(a1.extId) returns Some(a1)  
      mockAvalancheDao.selectAvalancheImageFilenames(a1.extId) returns Nil
      
      val req = openLiftReqBox(S.request)
      val jValue = responseAsJValue(AvyDetails(req)())

      extractJsonFieldValue(jValue, "extId") must_== a1.extId
    }
  }
}
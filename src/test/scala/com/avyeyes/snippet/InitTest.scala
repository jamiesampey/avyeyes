package com.avyeyes.snippet

import com.avyeyes.model.enums._
import com.avyeyes.test._

import bootstrap.liftweb.Boot
import net.liftweb.mocks.MockHttpServletRequest

class InitTest extends WebSpec2(Boot().boot _) with MockPersistence with AvalancheGenerator {
  "Initial JsCmd" should {
    val validExtId = "4jhu2ie9"
    val reqWithGoodExtId = new MockHttpServletRequest("http://avyeyes.com/" + validExtId)
    val reqWithBadExtId = new MockHttpServletRequest("http://avyeyes.com/j4ek-d3s")
    
    "Fly to an avalanche if passed a valid external ID" withSFor(reqWithGoodExtId) in {
      val initAvalancheLat = 35.59939321
      val initAvalancheLng = -104.323455342
      val initAvalanche = avalancheAtLocationWithAspect(validExtId, true, initAvalancheLat, initAvalancheLng, Aspect.E)
      
      mockAvalancheDao.selectViewableAvalanche(any[String]) returns Some(initAvalanche)
      
      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd
      
      there was one(mockAvalancheDao).selectViewableAvalanche(validExtId)
      initJsCalls must contain("avyeyes.overlaySearchResultKml")
      initJsCalls must contain(s"avyeyes.flyTo($initAvalancheLat,$initAvalancheLng,"
        + s"${init.InitAvyAltMeters},${init.InitAvyCamTilt},270)")
      autocompleteInitCallCount(initJsCalls) must_== 8
    }
    
    "Ignore an invalid external ID on the URL" withSFor(reqWithBadExtId) in {
      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd
      
      there was no(mockAvalancheDao).selectViewableAvalanche(any[String])
      initJsCalls must not contain("avyeyes.overlaySearchResultKml")
      initJsCalls must contain(s"avyeyes.flyTo(${init.InitViewLat},${init.InitViewLng},"
        + s"${init.InitViewAltMeters},${init.InitViewCamTilt},${init.InitViewHeading})")
      autocompleteInitCallCount(initJsCalls) must_== 8
    }
  }
  
  private def autocompleteInitCallCount(jsStr: String) = """\$\('\.[a-zA-Z]+'\)\.autocomplete""".r.findAllMatchIn(jsStr).length
}
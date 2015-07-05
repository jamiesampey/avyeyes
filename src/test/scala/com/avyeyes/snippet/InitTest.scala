package com.avyeyes.snippet

import bootstrap.liftweb.Boot
import com.avyeyes.test._

class InitTest extends WebSpec2(Boot().boot _) with MockInjectors with AvalancheHelpers {

  "Initial JsCmd" should {
    isolated 
    
    val validExtId = "4jhu2ie9"

    "Fly to an avalanche if passed a valid external ID" withSFor(s"http://avyeyes.com/$validExtId") in {
      val initAvalancheLat = 35.59939321
      val initAvalancheLng = -104.323455342
      val initAvalanche = avalancheWithCoords(validExtId, true, initAvalancheLat, initAvalancheLng, "")
      mockAvalancheDao.selectAvalanche(any[String]) returns Some(initAvalanche)

      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd

      there was one(mockAvalancheDao).selectAvalanche(validExtId)
      initJsCalls must contain("avyEyesView.addAvalancheAndFlyTo")
      initJsCalls must contain("avyEyesView.showModalDialog")
      autocompleteInitCallCount(initJsCalls) mustEqual 8
    }
    
    "Ignore an invalid external ID on the URL" withSFor("http://avyeyes.com/j4ek-d3s") in {
      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd

      there was no(mockAvalancheDao).selectAvalanche(any[String])
      initJsCalls must contain("avyEyesView.geolocateAndFlyTo")
      autocompleteInitCallCount(initJsCalls) mustEqual 8
    }

    "Initialize the view without an initial avalanche" withSFor("http://avyeyes.com") in {
      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd

      there was no(mockAvalancheDao).selectAvalanche(any[String])
      initJsCalls must contain("avyEyesView.geolocateAndFlyTo")
      autocompleteInitCallCount(initJsCalls) mustEqual 8
    }
  }
  
  private def autocompleteInitCallCount(jsStr: String) = """\$\('\.[a-zA-Z]+'\)\.autocomplete""".r.findAllMatchIn(jsStr).length
}
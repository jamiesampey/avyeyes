package com.avyeyes.snippet

import bootstrap.liftweb.Boot
import com.avyeyes.test._

class InitTest extends WebSpec2(Boot().boot _) with MockInjectors with Generators {

  "Initial JsCmd" should {
    isolated 
    
    val validExtId = "4jhu2ie9"

    "Fly to an avalanche if passed a valid external ID" withSFor(s"http://avyeyes.com/$validExtId") in {
      val initAvalanche = genAvalanche.sample.get.copy(extId = validExtId, viewable = true)
      mockAvalancheDao.getAvalanche(any[String]) returns Some(initAvalanche)

      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd

      there was one(mockAvalancheDao).getAvalanche(validExtId)
      initJsCalls must contain("avyEyesView.addAvalancheAndFlyTo")
      initJsCalls must contain("avyEyesView.showModalDialog")
      autocompleteInitCallCount(initJsCalls) mustEqual 8
    }
    
    "Ignore an invalid external ID on the URL" withSFor("http://avyeyes.com/j4ek-d3s") in {
      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd

      there was no(mockAvalancheDao).getAvalanche(any[String])
      initJsCalls must contain("avyEyesView.geolocateAndFlyTo")
      autocompleteInitCallCount(initJsCalls) mustEqual 8
    }

    "Initialize the view without an initial avalanche" withSFor("http://avyeyes.com") in {
      val init = new Init
      init.render
      val initJsCalls = init.initJsCalls().toJsCmd

      there was no(mockAvalancheDao).getAvalanche(any[String])
      initJsCalls must contain("avyEyesView.geolocateAndFlyTo")
      autocompleteInitCallCount(initJsCalls) mustEqual 8
    }
  }
  
  private def autocompleteInitCallCount(jsStr: String) = """\$\('\.[a-zA-Z]+'\)\.autocomplete""".r.findAllMatchIn(jsStr).length
}
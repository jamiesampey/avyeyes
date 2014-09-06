package com.avyeyes.snippet

import com.avyeyes.test._
import com.avyeyes.util.AEConstants._

class NotSupportedTest extends AvyEyesSpec {
  "Snippet rendering" should {
    "Wire message output span via CSS selector and list supported browser versions" withSFor("/") in {

      val notSupported = new NotSupported 
      val renderedPage = notSupported.render(WhaWhaHtmlElem)

      val n = (renderedPage \\ "span" filter (span => (span\"@id").text == "browserNotSupportedMsg")).head
      val notSupportedMsg = n.text 
      
      notSupportedMsg must contain(ChromeVersion.toString)
      notSupportedMsg must contain(FirefoxVersion.toString)
      notSupportedMsg must contain(OperaVersion.toString)
      notSupportedMsg must contain(SafariVersion.toString)
      notSupportedMsg must contain(IeVersion.toString)
    }
  }
}
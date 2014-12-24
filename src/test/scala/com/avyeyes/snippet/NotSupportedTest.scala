package com.avyeyes.snippet

import com.avyeyes.test._
import com.avyeyes.util.Constants._
import javax.servlet.http.HttpServletRequest
import net.liftweb.mocks.MockHttpServletRequest
import bootstrap.liftweb.Boot

class NotSupportedTest extends WebSpec2(Boot().boot _) with TemplateReader {
  "Snippet rendering" should {
    "Wire message output span via CSS selector and list supported browser versions" withSFor("/") in {

      val notSupported = new NotSupported 
      val renderedPage = notSupported.render(WhaWhaHtmlElem)

      val n = (renderedPage \\ "span" filter (span => (span\"@id").text == "browserNotSupportedMsg")).head
      val notSupportedMsg = n.text

      notSupportedMsg must contain(ChromeMinVersion.toString)
      notSupportedMsg must contain(FirefoxMinVersion.toString)
      notSupportedMsg must contain(OperaMinVersion.toString)
      notSupportedMsg must contain(IeMinVersion.toString)
    }
  }
  
  private def mockRequestFromUserAgent(browser: String, version: Double): HttpServletRequest = {
    val mockHttpServletRequest = new MockHttpServletRequest("/")
    mockHttpServletRequest.headers = Map("User-Agent" -> List(s"$browser/$version"))
    mockHttpServletRequest
  }
}
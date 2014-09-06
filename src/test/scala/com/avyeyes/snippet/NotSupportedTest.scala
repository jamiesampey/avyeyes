package com.avyeyes.snippet

import com.avyeyes.test._
import com.avyeyes.util.AEConstants._
import net.liftweb.mocks.MockHttpServletRequest
import net.liftweb.http.S
import net.liftweb.http.Req
import net.liftweb.http.GetRequest
import net.liftweb.common.Empty
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import net.liftweb.http.ParamCalcInfo
import javax.servlet.http.HttpServletRequest
import net.liftweb.sitemap.SiteMap
import bootstrap.liftweb.Boot
import net.liftweb.common.Full

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
  
  private def mockRequestFromUserAgent(browser: String, version: Double): HttpServletRequest = {
    val mockHttpServletRequest = new MockHttpServletRequest("/")
    mockHttpServletRequest.headers = Map("User-Agent" -> List(s"$browser/$version"))
    mockHttpServletRequest
  }
}
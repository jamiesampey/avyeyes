package com.avyeyes.snippet

import net.liftweb.util.Helpers.strToCssBindPromoter
import com.avyeyes.test.AvyEyesSpec
import scala.xml.XML
import scala.xml.NodeSeq
import net.liftweb.util.CssSel
import net.liftweb.http.SHtml

class SearchTest extends AvyEyesSpec {
  "Avalanche search snippet" should {
    "Wire input fields via CSS selectors" withSFor("/") in {

      val search = new Search    
      search.northLimit = "39.76999580282912"
      search.eastLimit = "-105.74790739483988"
      search.southLimit = "39.624208600404096"
      search.westLimit = "-106.0104492051601"
      
      search.camAlt = "7364.194647056396"
      search.camTilt = "39.94"
      search.camLat = "39.609381090080554"
      search.camLng = "-105.87917829999999"
      
      search.fromDate = "12-01-2013"
      search.toDate = "01-31-2014"
      search.avyType = "WL"
      search.avyTrigger = "N"
      search.rSize = "2"
      search.dSize = "3.5"
      search.numCaught = "2"
      search.numKilled = "1"
      
      val renderedPage = search.render(IndexHtmlElem)
      
      val HiddenInputType = "hidden"
      val TextInputType = "text"

      def assertInputValue(ns: NodeSeq, nodeType: String, cssSel: String, value: String) = {
        val node = (ns \\ "input" filter (node => (node\"@type").text == nodeType && (node\"@id").text == cssSel)).head
        (node\"@value").text must_== value
      }
      
      assertInputValue(renderedPage, HiddenInputType, "avySearchNorthLimit", search.northLimit)
      assertInputValue(renderedPage, HiddenInputType, "avySearchEastLimit", search.eastLimit)
      assertInputValue(renderedPage, HiddenInputType, "avySearchSouthLimit", search.southLimit)
      assertInputValue(renderedPage, HiddenInputType, "avySearchWestLimit", search.westLimit)
      assertInputValue(renderedPage, HiddenInputType, "avySearchCameraAlt", search.camAlt)
      assertInputValue(renderedPage, HiddenInputType, "avySearchCameraTilt", search.camTilt)
      assertInputValue(renderedPage, HiddenInputType, "avySearchCameraLat", search.camLat)
      assertInputValue(renderedPage, HiddenInputType, "avySearchCameraLng", search.camLng)
      assertInputValue(renderedPage, TextInputType, "avySearchFromDate", search.fromDate)
      assertInputValue(renderedPage, TextInputType, "avySearchToDate", search.toDate)
      assertInputValue(renderedPage, HiddenInputType, "avySearchType", search.avyType)
      assertInputValue(renderedPage, HiddenInputType, "avySearchTrigger", search.avyTrigger)
      assertInputValue(renderedPage, TextInputType, "avySearchRsizeValue", search.rSize)
      assertInputValue(renderedPage, TextInputType, "avySearchDsizeValue", search.dSize)
      assertInputValue(renderedPage, TextInputType, "avySearchNumCaught", search.numCaught)
      assertInputValue(renderedPage, TextInputType, "avySearchNumKilled", search.numKilled)
    }
  }
}
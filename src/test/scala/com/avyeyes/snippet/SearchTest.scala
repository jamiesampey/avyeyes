package com.avyeyes.snippet

import scala.xml.NodeSeq

import org.mockito.ArgumentCaptor

import com.avyeyes.persist._
import com.avyeyes.test.AvyEyesSpec
import com.avyeyes.util.AEConstants._

class SearchTest extends AvyEyesSpec {
  "Snippet rendering" should {
    "Wire input fields via CSS selectors" withSFor("/") in {

      val search = newSearchWithTestData 
      
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
  
  "Main search method" should {
    "Display 'eye too high' message if camera altitude is too high" withSFor("/") in {
      val search = new Search 
      search.camAlt = (CamRelAltLimitMeters + 1).toString
      val jsCmd = search.doSearch()
      
      jsCmd.toJsCmd must startWith("avyeyes.showModalDialog")
      jsCmd.toJsCmd must contain("Error")
    }
    
    "Pass search criteria to DAO" withSFor("/") in {
      mockAvalancheDao.selectAvalanches(any[AvalancheSearchCriteria]) returns Nil
      
      val searchCriteriaArg: ArgumentCaptor[AvalancheSearchCriteria] = 
        ArgumentCaptor.forClass(classOf[AvalancheSearchCriteria]);

      val search = newSearchWithTestData
      search.doSearch()
      search must not beNull

      there was one(mockAvalancheDao).selectAvalanches(searchCriteriaArg.capture())
      val passedCritera = searchCriteriaArg.getValue
      
      passedCritera.northLimit must_== search.northLimit
      passedCritera.eastLimit must_== search.eastLimit
      passedCritera.southLimit must_== search.southLimit
      passedCritera.westLimit must_== search.westLimit
      passedCritera.fromDateStr must_== search.fromDate
      passedCritera.toDateStr must_== search.toDate
      passedCritera.avyTypeStr must_== search.avyType
      passedCritera.avyTriggerStr must_== search.avyTrigger
      passedCritera.rSize must_== search.rSize
      passedCritera.dSize must_== search.dSize
      passedCritera.numCaught must_== search.numCaught
      passedCritera.numKilled must_== search.numKilled
    }
  }
  
  private def newSearchWithTestData(): Search = {
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
      
      search
  }
  
}
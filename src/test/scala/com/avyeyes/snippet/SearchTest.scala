package com.avyeyes.snippet

import com.avyeyes.data.{AvalancheQuery, CachedDAL}
import com.avyeyes.model.Coordinate
import com.avyeyes.model.enums._
import com.avyeyes.service.{Injectors, ResourceService}
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import com.avyeyes.util.Constants._
import com.avyeyes.util.Converters._
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

import scala.xml.NodeSeq

class SearchTest extends WebSpec2 with AroundExample with Mockito with TemplateReader {
  isolated

  val mockResources = mock[ResourceService]
  val mockAvalancheDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.dal.doWith(mockAvalancheDal) {
        AsResult(t)
      }
    }

  "Snippet rendering" >> {
    "Wire input fields via CSS selectors" withSFor "/" in {

      val search = newSearchWithTestData()
      
      val renderedPage = search.render(IndexHtmlElem)
      
      val HiddenInputType = "hidden"
      val TextInputType = "text"

      def assertInputValue(ns: NodeSeq, nodeType: String, cssSel: String, value: String) = {
        val n = (ns \\ "input" filter (node => (node\"@type").text == nodeType && (node\"@id").text == cssSel)).head
        (n\"@value").text mustEqual value
      }
      
      assertInputValue(renderedPage, HiddenInputType, "avySearchLatMax", search.latMax)
      assertInputValue(renderedPage, HiddenInputType, "avySearchLatMin", search.latMin)
      assertInputValue(renderedPage, HiddenInputType, "avySearchLngMax", search.lngMax)
      assertInputValue(renderedPage, HiddenInputType, "avySearchLngMin", search.lngMin)
      assertInputValue(renderedPage, HiddenInputType, "avySearchCameraAlt", search.camAlt)
      assertInputValue(renderedPage, HiddenInputType, "avySearchCameraPitch", search.camPitch)
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
  
  "Main search method" >> {

    "Display 'eye too high' message if camera altitude is too high" >> {
      mockResources.localizedString("title.errorDialog") returns "Error"

      val search = new Search 
      search.camAlt = (CamAltitudeLimit + 1).toString
      val jsCmd = search.doSearch()
      
      jsCmd.toJsCmd must startWith("avyEyesView.showModalDialog")
      jsCmd.toJsCmd must contain("Error")
    }
    
    "Pass search criteria to DAO" >> {
      mockAvalancheDal.getAvalanches(any[AvalancheQuery]) returns Nil
      
      val queryArg = capture[AvalancheQuery]

      val search = newSearchWithTestData()
      search.doSearch()

      there was one(mockAvalancheDal).getAvalanches(queryArg)
      val passedQuery = queryArg.value
      
      passedQuery.geoBounds.get.latMax mustEqual strToDblOrZero(search.latMax)
      passedQuery.geoBounds.get.latMin mustEqual strToDblOrZero(search.latMin)
      passedQuery.geoBounds.get.lngMax mustEqual strToDblOrZero(search.lngMax)
      passedQuery.geoBounds.get.lngMin mustEqual strToDblOrZero(search.lngMin)
      passedQuery.fromDate.get mustEqual strToDate(search.fromDate)
      passedQuery.toDate.get mustEqual strToDate(search.toDate)
      passedQuery.avyType.get mustEqual AvalancheType.withCode(search.avyType)
      passedQuery.trigger.get mustEqual AvalancheTrigger.withCode(search.avyTrigger)
      passedQuery.rSize.get mustEqual strToDblOrZero(search.rSize)
      passedQuery.dSize.get mustEqual strToDblOrZero(search.dSize)
      passedQuery.numCaught.get mustEqual strToIntOrNegOne(search.numCaught)
      passedQuery.numKilled.get mustEqual strToIntOrNegOne(search.numKilled)
    }
    
    "Does not use haversine distance if cam tilt is less than cutoff" >> {
      val avalancheInRange = avalancheForTest.copy(viewable = true,
        location = Coordinate(-105.875046142935, 39.6634870900582, 2500))
      
      val avalancheOutOfRange = avalancheForTest.copy(viewable = true,
        location = Coordinate(-103.875046142935, 41.6634870900582, 2500))
      
      mockAvalancheDal.getAvalanches(any[AvalancheQuery]) returns avalancheInRange :: avalancheOutOfRange :: Nil
      
      val search = newSearchWithTestData()
      search.camPitch = (CamPitchCutoff - 1).toString
      val jsCmd = search.doSearch()

      jsCmd.toJsCmd must contain(avalancheInRange.extId)
      jsCmd.toJsCmd must contain(avalancheOutOfRange.extId)
    }
    
    "Uses haversine distance if cam tilt is greater than cutoff" >> {
      val avalancheInRange = avalancheForTest.copy(viewable = true,
        location = Coordinate(-105.875046142935, 39.6634870900582, 2500))

      val avalancheOutOfRange = avalancheForTest.copy(viewable = true,
        location = Coordinate(-103.875046142935, 41.6634870900582, 2500))

      mockAvalancheDal.getAvalanches(any[AvalancheQuery]) returns avalancheInRange :: avalancheOutOfRange :: Nil
      
      val search = newSearchWithTestData()
      search.camPitch = (CamPitchCutoff + 1).toString
      val jsCmd = search.doSearch()

      jsCmd.toJsCmd must contain(avalancheInRange.extId)
      jsCmd.toJsCmd must not contain(avalancheOutOfRange.extId)
    }
  }
  
  private def newSearchWithTestData(): Search = {
      val search = new Search
      
      search.latMax = "39.76999580282912"
      search.latMin = "-105.74790739483988"
      search.lngMax = "39.624208600404096"
      search.lngMin = "-106.0104492051601"
      
      search.camAlt = "7364.194647056396"
      search.camPitch = "39.94"
      search.camLat = "39.609381090080554"
      search.camLng = "-105.87917829999999"
      
      search.fromDate = "12-01-2013"
      search.toDate = "01-31-2014"
      search.avyType = AvalancheType.WL.toString
      search.avyTrigger = AvalancheTrigger.N.toString
      search.rSize = "2"
      search.dSize = "3.5"
      search.numCaught = "2"
      search.numKilled = "1"
      
      search
  }
  
}
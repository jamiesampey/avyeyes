package avyeyes.snippet

import avyeyes.model.Avalanche
import avyeyes.model.AvalancheDb._
import avyeyes.model.enums._
import avyeyes.util.AEHelpers._
import avyeyes.util.AEConstants._
import avyeyes.util.ui.KmlCreator
import avyeyes.util.ui.JsDialog

import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.jquery.JqJE.JqAttr
import net.liftweb.common._
import net.liftweb.util.Helpers._

import scala.math._

import org.squeryl.PrimitiveTypeMode._


object AvySearch {
    val kmlCreator = new KmlCreator
    
    var northLimit = ""; var eastLimit = ""; var southLimit = ""; var westLimit = ""
    var camAlt = ""; var camTilt = ""; var camLat = ""; var camLng = "" 
    var fromDateStr = ""; var toDateStr = ""
    var avyType = ""; var trigger = ""; var rSize = ""; var dSize = ""
    var numCaught = ""; var numKilled = ""
        
	def render = {
		S.appendJs(populateAutoCompletes)
		
		"#avySearchNorthLimit" #> SHtml.hidden(northLimit = _, northLimit) &
		"#avySearchEastLimit" #> SHtml.hidden(eastLimit = _, eastLimit) &
		"#avySearchSouthLimit" #> SHtml.hidden(southLimit = _, southLimit) &
		"#avySearchWestLimit" #> SHtml.hidden(westLimit = _, westLimit) &
		"#avySearchCameraAlt" #> SHtml.hidden(camAlt = _, camAlt) &
		"#avySearchCameraTilt" #> SHtml.hidden(camTilt = _, camTilt) &
		"#avySearchCameraLat" #> SHtml.hidden(camLat = _, camLat) &
		"#avySearchCameraLng" #> SHtml.hidden(camLng = _, camLng) &
		"#avySearchFromDate" #> SHtml.text(fromDateStr, fromDateStr = _) &
		"#avySearchToDate" #> SHtml.text(toDateStr, toDateStr = _) &
		"#avySearchType" #> SHtml.hidden(avyType = _, avyType) &
		"#avySearchTrigger" #> SHtml.hidden(trigger = _, trigger) &
		"#avySearchRsizeValue" #> SHtml.text(rSize, rSize = _) &
		"#avySearchDsizeValue" #> SHtml.text(dSize, dSize = _) &
		"#avySearchNumCaught" #> SHtml.text(numCaught, numCaught = _) &
		"#avySearchNumKilled" #> SHtml.text(numKilled, numKilled = _) & 
		"#avySearchSubmitBinding" #> SHtml.hidden(doSearch)
	}

    private def doSearch(): JsCmd =
      if (strToDbl(camAlt) > CAM_REL_ALT_LIMIT_METERS)
          JsDialog.error("Eye altitude is too high. Eye must be less than 15,000 feet above the ground.")
      else {
        val kml = kmlCreator.createCompositeKml(matchingAvalanchesInRange)
        
        Call("view.overlaySearchResultKml", kml.toString).cmd &
        JsDialog.info("Found " + matchingAvalanchesInRange.size 
            + " avalanches within the current view that match the search criteria. Click on an avalanche for details.")
      }
    
    private def matchingAvalanchesInRange: List[Avalanche] = transaction {
        if (strToDbl(camTilt) < CAM_TILT_RANGE_CUTOFF) 
          matchingAvalanches.toList 
        else
          matchingAvalanches.toList filter (a => haversineDist(a) < AVY_DIST_RANGE_MILES)
    }
	
    private def matchingAvalanches = {
        val fromDate = if (!fromDateStr.isEmpty) parseDateStr(fromDateStr) else earliestAvyDate
	    val toDate = if (!toDateStr.isEmpty) parseDateStr(toDateStr) else today.getTime
	  
        from(avalanchesInView)(a => where(
            a.avyDate.between(fromDate, toDate)
        	and (a.avyType === AvalancheType.withCode(avyType)).inhibitWhen(avyType.isEmpty)
        	and (a.trigger === AvalancheTrigger.withCode(trigger)).inhibitWhen(trigger.isEmpty)
        	and (a.rSize gte getAvySizeQueryVal(rSize).?)
        	and (a.dSize gte getAvySizeQueryVal(dSize).?)
        	and (a.caught gte getHumanNumberQueryVal(numCaught).?)
        	and (a.killed gte getHumanNumberQueryVal(numKilled).?))
        select(a))
	}
        
  	private def avalanchesInView = {
		val latBounds = List(strToDbl(northLimit), strToDbl(southLimit))
		val lngBounds = List(strToDbl(eastLimit), strToDbl(westLimit))

		from(avalanches)(a => where(a.lat.between(latBounds.min, latBounds.max)
			and a.lng.between(lngBounds.min, lngBounds.max)) select(a))
  	}
  	
  	private def getAvySizeQueryVal(sizeStr: String): Option[Double] = 
  	  if (strToDbl(sizeStr) > 0) Some(strToDbl(sizeStr)) else None
  	
  	private def getHumanNumberQueryVal(numStr: String): Option[Int] = 
  	  if (strToHumanNumber(numStr) >= 0) Some(strToHumanNumber(numStr)) else None

  	private def haversineDist(a: Avalanche) = {
      val dLat = (a.lat - strToDbl(camLat)).toRadians
      val dLon = (a.lng - strToDbl(camLng)).toRadians

      val ax = pow(sin(dLat/2),2) + pow(sin(dLon/2),2) * cos(strToDbl(camLat).toRadians) * cos(a.lat.toRadians)
      val c = 2 * asin(sqrt(ax))
      EARTH_RADIUS_MILES * c
  	}
  	 
    private def populateAutoCompletes = JsRaw("""
        document.addEventListener('avyEyesViewInit', function(e) {
                $('.avyTypeAutoComplete').autocomplete('option', 'source', """ + AvalancheType.toJsonArray  + """);
                $('.avyTriggerAutoComplete').autocomplete('option', 'source', """ + AvalancheTrigger.toJsonArray  + """);
                $('.avySkyAutoComplete').autocomplete('option', 'source', """ + Sky.toJsonArray  + """);
                $('.avyPrecipAutoComplete').autocomplete('option', 'source', """ + Precip.toJsonArray  + """);
                $('.avyInterfaceAutoComplete').autocomplete('option', 'source', """ + AvalancheInterface.toJsonArray  + """);
                $('.avyAspectAutoComplete').autocomplete('option', 'source', """ + Aspect.toJsonArray  + """);
                $('.avyModeOfTravelAutoComplete').autocomplete('option', 'source', """ + ModeOfTravel.toJsonArray  + """);
            });""")
}

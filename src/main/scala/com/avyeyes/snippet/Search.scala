package com.avyeyes.snippet

import scala.math._

import org.squeryl.PrimitiveTypeMode._

import com.avyeyes.model.Avalanche
import com.avyeyes.model.AvalancheDb._
import com.avyeyes.model.enums._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.ui.JsDialog
import com.avyeyes.util.ui.KmlCreator
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._

class Search {
    private val kmlCreator = new KmlCreator

    var northLimit = ""; var eastLimit = ""; var southLimit = ""; var westLimit = ""
    var camAlt = ""; var camTilt = ""; var camLat = ""; var camLng = "" 
    var fromDateStr = ""; var toDateStr = ""
    var avyType = ""; var trigger = ""; var rSize = ""; var dSize = ""
    var numCaught = ""; var numKilled = ""
    
	def render = {
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
          JsDialog.error("eyeTooHigh")
      else {
        val kml = kmlCreator.createCompositeKml(matchingAvalanchesInRange:_*)
        
        Call("view.overlaySearchResultKml", kml.toString).cmd &
        JsDialog.info("avySearchSuccess", matchingAvalanchesInRange.size)
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
        	and (a.avyType === AvalancheType.withName(avyType)).inhibitWhen(avyType.isEmpty)
        	and (a.trigger === AvalancheTrigger.withName(trigger)).inhibitWhen(trigger.isEmpty)
        	and (a.rSize gte getAvySizeQueryVal(rSize).?)
        	and (a.dSize gte getAvySizeQueryVal(dSize).?)
        	and (a.caught gte getHumanNumberQueryVal(numCaught).?)
        	and (a.killed gte getHumanNumberQueryVal(numKilled).?))
        select(a))
	}
        
  	private def avalanchesInView = {
		val latBounds = List(strToDbl(northLimit), strToDbl(southLimit))
		val lngBounds = List(strToDbl(eastLimit), strToDbl(westLimit))

		from(avalanches)(a => where(a.viewable === true 
		    and a.lat.between(latBounds.min, latBounds.max)
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
}

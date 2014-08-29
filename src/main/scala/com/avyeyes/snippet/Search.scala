package com.avyeyes.snippet

import scala.math._

import org.squeryl.PrimitiveTypeMode.transaction

import com.avyeyes.model.Avalanche
import com.avyeyes.model._
import com.avyeyes.persist.SquerylPersistence
import com.avyeyes.service.AvalancheSearchCriteria
import com.avyeyes.service.AvalancheService
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers.strToDbl
import com.avyeyes.util.ui.JsDialog
import com.avyeyes.util.ui.KmlCreator

import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsExp.strToJsExp
import net.liftweb.util.Helpers.strToCssBindPromoter

class Search extends AvalancheService with SquerylPersistence {
  private val kmlCreator = new KmlCreator

  var northLimit = ""; var eastLimit = ""; var southLimit = ""; var westLimit = ""
  var camAlt = ""; var camTilt = ""; var camLat = ""; var camLng = "" 
  var fromDate = ""; var toDate = ""
  var avyType = ""; var avyTrigger = ""; var rSize = ""; var dSize = ""
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
		"#avySearchFromDate" #> SHtml.text(fromDate, fromDate = _) &
		"#avySearchToDate" #> SHtml.text(toDate, toDate = _) &
		"#avySearchType" #> SHtml.hidden(avyType = _, avyType) &
		"#avySearchTrigger" #> SHtml.hidden(avyTrigger = _, avyTrigger) &
		"#avySearchRsizeValue" #> SHtml.text(rSize, rSize = _) &
		"#avySearchDsizeValue" #> SHtml.text(dSize, dSize = _) &
		"#avySearchNumCaught" #> SHtml.text(numCaught, numCaught = _) &
		"#avySearchNumKilled" #> SHtml.text(numKilled, numKilled = _) & 
		"#avySearchSubmitBinding" #> SHtml.hidden(doSearch)
	}

  private def doSearch(): JsCmd = {
    if (strToDbl(camAlt) > CamRelAltLimitMeters)
        JsDialog.error("eyeTooHigh")
    else {
      val avyList = matchingAvalanchesInRange
      val kml = kmlCreator.createCompositeKml(avyList:_*)
      
      logger.debug(s"Found ${avyList.size} avalanches matching criteria "
          + s" [From: $fromDate | To: $toDate | Type: $avyType | Trigger: $avyTrigger"
          + s" | R size: $rSize | D size: $dSize | Caught: $numCaught | Killed: $numKilled]")

      Call("avyeyes.overlaySearchResultKml", kml.toString).cmd &
      JsDialog.info("avySearchSuccess", avyList.size)
    }
  }

  private def matchingAvalanchesInRange: List[Avalanche] = {
    val criteria = AvalancheSearchCriteria(northLimit, eastLimit, southLimit, westLimit, 
      fromDate, toDate, avyType, avyTrigger, rSize, dSize, numCaught, numKilled)
      
    var matchingAvalanches: List[Avalanche] = Nil
    transaction {
      matchingAvalanches = findAvalanches(criteria)
    }
    
    if (strToDbl(camTilt) < CamTiltRangeCutoff) 
      matchingAvalanches
    else 
      matchingAvalanches filter (a => haversineDist(a) < AvyDistRangeMiles)
  }

	private def haversineDist(a: Avalanche) = {
    val dLat = (a.lat - strToDbl(camLat)).toRadians
    val dLon = (a.lng - strToDbl(camLng)).toRadians

    val ax = pow(sin(dLat/2),2) + pow(sin(dLon/2),2) * cos(strToDbl(camLat).toRadians) * cos(a.lat.toRadians)
    val c = 2 * asin(sqrt(ax))
    EarthRadiusMiles * c
	}
}

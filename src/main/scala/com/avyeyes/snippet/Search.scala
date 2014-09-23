package com.avyeyes.snippet

import scala.math._

import org.squeryl.PrimitiveTypeMode.transaction

import com.avyeyes.model._
import com.avyeyes.persist._
import com.avyeyes.service.KmlCreator
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.JsDialog

import net.liftweb.common.Loggable
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsExp.strToJsExp
import net.liftweb.util.Helpers._

class Search extends KmlCreator with Loggable {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  
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

  def doSearch(): JsCmd = {
    if (strToDblOrZero(camAlt) > CamRelAltLimitMeters)
        JsDialog.error("eyeTooHigh")
    else {
      val avyList = matchingAvalanchesInRange
      
      logger.debug(s"Found ${avyList.size} avalanches matching criteria "
          + s" [From: $fromDate | To: $toDate | Type: $avyType | Trigger: $avyTrigger"
          + s" | R size: $rSize | D size: $dSize | Caught: $numCaught | Killed: $numKilled]")

      if (avyList.size > 0) {
        val kml = createCompositeKml(avyList:_*)
        Call("avyeyes.overlaySearchResultKml", kml.toString).cmd & JsDialog.info("avySearchSuccess", avyList.size)
      } else {
        JsDialog.info("avySearchZeroMatches")
      } 
    }
  }

  private def matchingAvalanchesInRange: List[Avalanche] = {
    val criteria = AvalancheSearchCriteria(northLimit, eastLimit, southLimit, westLimit, 
      fromDate, toDate, avyType, avyTrigger, rSize, dSize, numCaught, numKilled)
      
    val matchingAvalanches: List[Avalanche] = transaction {
      dao.selectAvalanches(criteria)
    }
    
    if (strToDblOrZero(camTilt) < CamTiltRangeCutoff) 
      matchingAvalanches
    else 
      matchingAvalanches filter (a => haversineDist(a) < AvyDistRangeMiles)
  }

	private def haversineDist(a: Avalanche) = {
    val dLat = (a.lat - strToDblOrZero(camLat)).toRadians
    val dLon = (a.lng - strToDblOrZero(camLng)).toRadians

    val ax = pow(sin(dLat/2),2) + pow(sin(dLon/2),2) * cos(strToDblOrZero(camLat).toRadians) * cos(a.lat.toRadians)
    val c = 2 * asin(sqrt(ax))
    EarthRadiusMiles * c
	}
}

package com.avyeyes.snippet

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvyEyesSqueryl.transaction
import com.avyeyes.persist._
import com.avyeyes.service.{DependencyInjector, KmlCreator}
import com.avyeyes.util.Constants._
import com.avyeyes.util.Helpers._
import com.avyeyes.util.JsDialog
import net.liftweb.common.Loggable
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsExp.strToJsExp
import net.liftweb.util.Helpers._
import org.apache.commons.lang3.StringUtils._

import scala.math._

class Search extends KmlCreator with Loggable {
  lazy val dao = DependencyInjector.avalancheDao.vend
  
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
        Call("avyeyes.overlaySearchResultKml", kml.toString).cmd &
        Call("avyeyes.hideSearchDiv").cmd &
        JsDialog.info("avySearchSuccess", avyList.size)
      } else {
        JsDialog.info("avySearchZeroMatches")
      } 
    }
  }
    
  private def matchingAvalanchesInRange: List[Avalanche] = {
    val query = AvalancheQuery(
      viewable = Some(true), 
      geo = Some(GeoBounds(northLimit, eastLimit, southLimit, westLimit)), 
      fromDate = if (isNotBlank(fromDate)) Some(strToDate(fromDate)) else None, 
      toDate = if (isNotBlank(toDate)) Some(strToDate(toDate)) else None, 
      avyType = if (isNotBlank(avyType)) Some(AvalancheType.withName(avyType)) else None, 
      avyTrigger = if (isNotBlank(avyTrigger)) Some(AvalancheTrigger.withName(avyTrigger)) else None, 
      rSize = getAvySizeQueryVal(rSize), 
      dSize = getAvySizeQueryVal(dSize), 
      numCaught = getHumanNumberQueryVal(numCaught), 
      numKilled = getHumanNumberQueryVal(numKilled))
      
    val matchingAvalanches: List[Avalanche] = transaction {
      dao.selectAvalanches(query)
    }
    
    if (strToDblOrZero(camTilt) < CamTiltRangeCutoff)  {
      matchingAvalanches
    } else { 
      matchingAvalanches filter (a => haversineDist(a) < AvyDistRangeMiles)
    }
  }

  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = {
    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
  }
  
  private def getHumanNumberQueryVal(numStr: String): Option[Int] = {
    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
  }
  
	private def haversineDist(a: Avalanche) = {
    val dLat = (a.lat - strToDblOrZero(camLat)).toRadians
    val dLon = (a.lng - strToDblOrZero(camLng)).toRadians

    val ax = pow(sin(dLat/2),2) + pow(sin(dLon/2),2) * cos(strToDblOrZero(camLat).toRadians) * cos(a.lat.toRadians)
    val c = 2 * asin(sqrt(ax))
    EarthRadiusMiles * c
	}
}

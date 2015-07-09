package com.avyeyes.snippet

import com.avyeyes.data._
import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.util.Constants._
import com.avyeyes.util.Helpers._
import com.avyeyes.util.JsDialog
import net.liftweb.common.Loggable
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmd
import net.liftweb.json.JsonAST.JArray
import net.liftweb.util.Helpers._
import org.apache.commons.lang3.StringUtils._

import scala.math._

class Search extends Loggable {
  lazy val dao = DaoInjector.dao.vend
  
  var latMax = ""; var latMin = ""; var lngMax = ""; var lngMin = ""
  var camAlt = ""; var camPitch = ""; var camLat = ""; var camLng = ""
  var fromDate = ""; var toDate = ""
  var avyType = ""; var avyTrigger = ""; var rSize = ""; var dSize = ""
  var numCaught = ""; var numKilled = ""
    
	def render = {
		"#avySearchLatMax" #> SHtml.hidden(latMax = _, latMax) &
		"#avySearchLatMin" #> SHtml.hidden(latMin = _, latMin) &
		"#avySearchLngMax" #> SHtml.hidden(lngMax = _, lngMax) &
		"#avySearchLngMin" #> SHtml.hidden(lngMin = _, lngMin) &
		"#avySearchCameraAlt" #> SHtml.hidden(camAlt = _, camAlt) &
		"#avySearchCameraPitch" #> SHtml.hidden(camPitch = _, camPitch) &
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
    val camAltitude = strToDblOrZero(camAlt).toInt
    if (camAltitude > CamAltitudeLimit) {
      JsDialog.error("eyeTooHigh", CamAltitudeLimit, camAltitude)
    } else if (Seq(latMax, latMin, lngMax, lngMin).exists(_.isEmpty)) {
      JsDialog.error("horizonInView")
    } else {
      val avyList = matchingAvalanchesInRange
      
      logger.debug(s"Found ${avyList.size} avalanches matching criteria "
          + s" [From: $fromDate | To: $toDate | Type: $avyType | Trigger: $avyTrigger"
          + s" | R size: $rSize | D size: $dSize | Caught: $numCaught | Killed: $numKilled]")

      if (avyList.size > 0) {
        Call("avyEyesView.addAvalanches", JArray(avyList.map(_.toSearchResultJson))).cmd &
        JsDialog.info("avySearchSuccess", avyList.size)
      } else {
        JsDialog.info("avySearchZeroMatches")
      } 
    }
  }
    
  private def matchingAvalanchesInRange: List[Avalanche] = {
    val matchingAvalanches = dao.getAvalanches(
      AvalancheQuery(
        viewable = Some(true),
        geoBounds = Some(GeoBounds(strToDblOrZero(latMax), strToDblOrZero(latMin),
          strToDblOrZero(lngMax), strToDblOrZero(lngMin))),
        fromDate = if (isNotBlank(fromDate)) Some(strToDate(fromDate)) else None,
        toDate = if (isNotBlank(toDate)) Some(strToDate(toDate)) else None,
        avyType = if (isNotBlank(avyType)) Some(AvalancheType.withName(avyType)) else None,
        trigger = if (isNotBlank(avyTrigger)) Some(AvalancheTrigger.withName(avyTrigger)) else None,
        rSize = getAvySizeQueryVal(rSize),
        dSize = getAvySizeQueryVal(dSize),
        numCaught = getHumanNumberQueryVal(numCaught),
        numKilled = getHumanNumberQueryVal(numKilled),
        orderBy =  List((OrderField.date, OrderDirection.desc))
    ))

    if (strToDblOrZero(camPitch) > CamPitchCutoff)  {
      matchingAvalanches
    } else { 
      matchingAvalanches.filter(a => haversineDist(a) < AvyDistRangeMiles)
    }
  }

  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = {
    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
  }
  
  private def getHumanNumberQueryVal(numStr: String): Option[Int] = {
    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
  }
  
	private def haversineDist(a: Avalanche) = {
    val dLat = (a.location.latitude - strToDblOrZero(camLat)).toRadians
    val dLon = (a.location.longitude - strToDblOrZero(camLng)).toRadians

    val ax = pow(sin(dLat/2),2) + pow(sin(dLon/2),2) * cos(strToDblOrZero(camLat).toRadians) *
      cos(a.location.latitude.toRadians)
    val c = 2 * asin(sqrt(ax))
    EarthRadiusMiles * c
	}
}

package com.avyeyes.snippet

import com.avyeyes.data._
import com.avyeyes.model._
import com.avyeyes.model.JsonSerializers._
import com.avyeyes.model.enums._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants._
import com.avyeyes.util.Converters._
import net.liftweb.common.Loggable
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmd
import net.liftweb.json.JsonAST.JArray
import net.liftweb.util.Helpers._
import org.apache.commons.lang3.StringUtils._

class Search extends ModalDialogs with Loggable {
  val R = Injectors.resources.vend
  val dal = Injectors.dal.vend

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
      errorDialog("eyeTooHigh", CamAltitudeLimit)
    } else if (Seq(latMax, latMin, lngMax, lngMin).exists(_.isEmpty)) {
      errorDialog("horizonInView")
    } else {
      val avyList = matchingAvalanchesInRange
      
      logger.debug(s"Found ${avyList.size} avalanches matching criteria "
          + s" [From: $fromDate | To: $toDate | Type: $avyType | Trigger: $avyTrigger"
          + s" | R size: $rSize | D size: $dSize | Caught: $numCaught | Killed: $numKilled]")

      if (avyList.size > 0) {
        Call("avyEyesView.addAvalanches", JArray(avyList.map(avalancheSearchResult))).cmd &
        infoDialog("avySearchSuccess", avyList.size)
      } else {
        infoDialog("avySearchZeroMatches")
      } 
    }
  }
    
  private def matchingAvalanchesInRange: List[Avalanche] = {
    val matchingAvalanches = dal.getAvalanches(
      AvalancheQuery(
        viewable = Some(true),
        geoBounds = Some(GeoBounds(
          lngMax = strToDblOrZero(lngMax),
          lngMin = strToDblOrZero(lngMin),
          latMax = strToDblOrZero(latMax),
          latMin = strToDblOrZero(latMin))),
        fromDate = if (isNotBlank(fromDate)) Some(strToDate(fromDate)) else None,
        toDate = if (isNotBlank(toDate)) Some(strToDate(toDate)) else None,
        avyType = if (isNotBlank(avyType)) Some(AvalancheType.fromCode(avyType)) else None,
        trigger = if (isNotBlank(avyTrigger)) Some(AvalancheTrigger.fromCode(avyTrigger)) else None,
        rSize = getAvySizeQueryVal(rSize),
        dSize = getAvySizeQueryVal(dSize),
        numCaught = getHumanNumberQueryVal(numCaught),
        numKilled = getHumanNumberQueryVal(numKilled),
        order =  List((OrderField.Date, OrderDirection.desc))
    ))

    if (strToDblOrZero(camPitch) > CamPitchCutoff)  {
      val camLocation = Coordinate(strToDblOrZero(camLng), strToDblOrZero(camLat), 0)
      matchingAvalanches.filter(_.location.distanceTo(camLocation) < AvyDistRangeMiles)
    } else {
      matchingAvalanches
    }
  }

  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = {
    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
  }
  
  private def getHumanNumberQueryVal(numStr: String): Option[Int] = {
    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
  }
}

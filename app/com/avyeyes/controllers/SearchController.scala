package com.avyeyes.controllers

//import javax.inject.Inject
//
//import com.avyeyes.data.{CachedDAL, MemoryMapCachedDAL}
//import com.avyeyes.model.{Avalanche, JsonSerializers}
//import org.json4s.Formats
//import play.api.Logger
//import play.api.mvc.{Action, Controller, Result}
//import org.json4s.jackson.Serialization._
//import java.text.NumberFormat
//import java.util.Locale
//
//import com.avyeyes.data._
//import com.avyeyes.model._
//import com.avyeyes.model.JsonSerializers._
//import com.avyeyes.model.enums._
//import com.avyeyes.service.Injectors
//import com.avyeyes.util.Constants._
//import com.avyeyes.util.Converters._
//import net.liftweb.common.Loggable
//import net.liftweb.http.SHtml
//import net.liftweb.http.js.JE.Call
//import net.liftweb.http.js.JsCmd
//import net.liftweb.json.JsonAST.JArray
//import net.liftweb.util.Helpers._
//import org.apache.commons.lang3.StringUtils._

//class SearchController @Inject()(dal: MemoryMapCachedDAL, jsonSerializers: JsonSerializers, logger: Logger) extends Controller {
//  protected val R = Injectors.resources.vend
//  private val dal = Injectors.dal.vend
//
//  var latMax = ""; var latMin = ""; var lngMax = ""; var lngMin = ""
//  var camAlt = ""; var camPitch = ""; var camLat = ""; var camLng = ""
//  var fromDate = ""; var toDate = ""
//  var avyType = ""; var avyTrigger = ""; var rSize = ""; var dSize = ""
//  var numCaught = ""; var numKilled = ""
//
//	def render = {
//		"#avySearchLatMax" #> SHtml.hidden(latMax = _, latMax) &
//		"#avySearchLatMin" #> SHtml.hidden(latMin = _, latMin) &
//		"#avySearchLngMax" #> SHtml.hidden(lngMax = _, lngMax) &
//		"#avySearchLngMin" #> SHtml.hidden(lngMin = _, lngMin) &
//		"#avySearchCameraAlt" #> SHtml.hidden(camAlt = _, camAlt) &
//		"#avySearchCameraPitch" #> SHtml.hidden(camPitch = _, camPitch) &
//		"#avySearchCameraLat" #> SHtml.hidden(camLat = _, camLat) &
//		"#avySearchCameraLng" #> SHtml.hidden(camLng = _, camLng) &
//		"#avySearchFromDate" #> SHtml.text(fromDate, fromDate = _) &
//		"#avySearchToDate" #> SHtml.text(toDate, toDate = _) &
//		"#avySearchType" #> SHtml.hidden(avyType = _, avyType) &
//		"#avySearchTrigger" #> SHtml.hidden(avyTrigger = _, avyTrigger) &
//		"#avySearchRsizeValue" #> SHtml.text(rSize, rSize = _) &
//		"#avySearchDsizeValue" #> SHtml.text(dSize, dSize = _) &
//		"#avySearchNumCaught" #> SHtml.text(numCaught, numCaught = _) &
//		"#avySearchNumKilled" #> SHtml.text(numKilled, numKilled = _) &
//		"#avySearchSubmitBinding" #> SHtml.hidden(doSearch)
//	}
//


//  def doSearch(): JsCmd = if (strToDblOrZero(camAlt).toInt > CamAltitudeLimit) {
//    errorDialog("eyeTooHigh", NumberFormat.getNumberInstance(Locale.US).format(CamAltitudeLimit))
//  } else if (Seq(latMax, latMin, lngMax, lngMin).exists(_.isEmpty)) {
//    errorDialog("horizonInView")
//  } else {
//    val avyList = matchingAvalanchesInRange
//
//    logger.debug(s"Found ${avyList.size} avalanches matching criteria "
//        + s" [From: $fromDate | To: $toDate | Type: $avyType | Trigger: $avyTrigger"
//        + s" | R size: $rSize | D size: $dSize | Caught: $numCaught | Killed: $numKilled]")
//
//    avyList match {
//      case Nil => infoDialog("avySearchZeroMatches")
//      case _ => Call("avyEyesView.addAvalanches", JArray(avyList.map(avalancheSearchResultData))).cmd
//    }
//  }
//
//  private def matchingAvalanchesInRange: List[Avalanche] = {
//    val matchingAvalanches = dal.getAvalanches(
//      AvalancheQuery(
//        viewable = Some(true),
//        geoBounds = Some(GeoBounds(
//          lngMax = strToDblOrZero(lngMax),
//          lngMin = strToDblOrZero(lngMin),
//          latMax = strToDblOrZero(latMax),
//          latMin = strToDblOrZero(latMin))),
//        fromDate = if (isNotBlank(fromDate)) Some(strToDate(fromDate)) else None,
//        toDate = if (isNotBlank(toDate)) Some(strToDate(toDate)) else None,
//        avyType = if (isNotBlank(avyType)) Some(AvalancheType.fromCode(avyType)) else None,
//        trigger = if (isNotBlank(avyTrigger)) Some(AvalancheTrigger.fromCode(avyTrigger)) else None,
//        rSize = getAvySizeQueryVal(rSize),
//        dSize = getAvySizeQueryVal(dSize),
//        numCaught = getHumanNumberQueryVal(numCaught),
//        numKilled = getHumanNumberQueryVal(numKilled),
//        order =  List((OrderField.Date, OrderDirection.desc))
//    ))
//
//    strToDblOrZero(camPitch) match {
//      case numericalCamPitch if numericalCamPitch > CamPitchCutoff =>
//        val camLocation = Coordinate(strToDblOrZero(camLng), strToDblOrZero(camLat), 0)
//        matchingAvalanches.filter(_.location.distanceTo(camLocation) < AvyDistRangeMiles)
//      case _ => matchingAvalanches
//    }
//  }
//
//  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = {
//    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
//  }
//
//  private def getHumanNumberQueryVal(numStr: String): Option[Int] = {
//    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
//  }
//}

package com.avyeyes.snippet

import com.avyeyes.model.Avalanche
import com.avyeyes.model.AvalancheDb
import com.avyeyes.model.enums._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.ui.JsDialog
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.util.Props
import org.squeryl.PrimitiveTypeMode._
import scala.xml.XML
import org.apache.commons.lang3.StringUtils.isBlank

class Report {
  var extId = AvalancheDb.reserveNewExtId
  
  var submitterEmail = ""; var submitterExp = ""; var submitterYearsExp = ""
  var lat = ""; var lng = ""; 
  var areaName = ""; var dateStr = ""; var sky = ""; var precip = ""
  var elevation = ""; var aspect = ""; var angle = ""    
  var avyType = ""; var trigger = ""; var bedSurface = ""; var rSize = ""; var dSize = ""
  var caught = ""; var partiallyBuried = ""; var fullyBuried = ""; var injured = ""; var killed = ""
  var modeOfTravel = ""; var comments = ""; var kmlStr = ""
      
  def render = {
    "#avyReportExtId" #> SHtml.hidden(extId = _, extId) &
    "#avyReportLat" #> SHtml.hidden(lat = _, "") &
    "#avyReportLng" #> SHtml.hidden(lng = _, "") &
    "#avyReportAreaName" #> SHtml.text("", areaName = _) &
    "#avyReportDate" #> SHtml.text("", dateStr = _) &
    "#avyReportSky" #> SHtml.hidden(sky = _, "") &
    "#avyReportPrecip" #> SHtml.hidden(precip = _, "") &
    "#avyReportElevation" #> SHtml.text("", elevation = _) &
    "#avyReportAspect" #> SHtml.hidden(aspect = _, "") &
    "#avyReportAngle" #> SHtml.text("", angle = _) &
    "#avyReportType" #> SHtml.hidden(avyType = _, "") & 
    "#avyReportTrigger" #> SHtml.hidden(trigger = _, "") &
    "#avyReportBedSurface" #> SHtml.hidden(bedSurface = _, "") &
    "#avyReportRsizeValue" #> SHtml.text("", rSize = _) &
    "#avyReportDsizeValue" #> SHtml.text("", dSize = _) &
    "#avyReportNumCaught" #> SHtml.text("", caught = _) &
    "#avyReportNumPartiallyBuried" #> SHtml.text("", partiallyBuried = _) &
    "#avyReportNumFullyBuried" #> SHtml.text("", fullyBuried = _) &
    "#avyReportNumInjured" #> SHtml.text("", injured = _) &
    "#avyReportNumKilled" #> SHtml.text("", killed = _) &
    "#avyReportModeOfTravel" #> SHtml.hidden(modeOfTravel = _, "") &
    "#avyReportComments" #> SHtml.textarea("", comments = _) &
    "#avyReportSubmitterEmail" #> SHtml.text("", submitterEmail = _) &
    "#avyReportSubmitterExp" #> SHtml.hidden(submitterExp = _, "") &
    "#avyReportSubmitterYearsExp" #> SHtml.text("", submitterYearsExp = _) &
    "#avyReportKml" #> SHtml.hidden(kmlStr = _, "") &
    "#avyReportSubmitBinding" #> SHtml.hidden(doReport)
  }
  
  private def doReport() = {
      try {
         checkAutoCompleteValues
         val kmlCoordsNode = (XML.loadString(kmlStr) \\ "LinearRing" \ "coordinates").head
    	 
         transaction {
    	      val newAvalanche = new Avalanche(extId, false, 
    	          submitterEmail, ExperienceLevel.withName(submitterExp), asInt(submitterYearsExp) openOr 0,
    	          asDouble(lat) openOr 0, asDouble(lng) openOr 0, 
    	          areaName, parseDateStr(dateStr), Sky.withName(sky), Precip.withName(precip), 
    	          asInt(elevation) openOr -1, Aspect.withName(aspect), asInt(angle) openOr -1, 
    	    	  AvalancheType.withName(avyType), AvalancheTrigger.withName(trigger), 
    	    	  AvalancheInterface.withName(bedSurface), asDouble(rSize) openOr 0, asDouble(dSize) openOr 0, 
    	    	  asInt(caught) openOr -1, asInt(partiallyBuried) openOr -1, asInt(fullyBuried) openOr -1, 
    	    	  asInt(injured) openOr -1, asInt(killed) openOr -1, 
    	    	  ModeOfTravel.withName(modeOfTravel), Some(comments), kmlCoordsNode.text.trim)
    	  
	    	  AvalancheDb.avalanches insert newAvalanche
	      }
	      
	      JsDialog.info("avyReportSuccess", Props.get("base.url").get + extId)
      } catch {

          case e: Exception => {
            JsDialog.error("avyReportError", e.getMessage())
            System.out.println(e.printStackTrace)
          }
      } finally {
          AvalancheDb.unreserveExtId(extId)
      }
  }
  
  private def checkAutoCompleteValues = {
         if (isBlank(aspect)) aspect = Aspect.N.toString
         if (isBlank(submitterExp)) submitterExp = ExperienceLevel.A0.toString
         if (isBlank(sky)) sky = Sky.U.toString
         if (isBlank(precip)) precip = Precip.U.toString
         if (isBlank(avyType)) avyType = AvalancheType.U.toString
         if (isBlank(trigger)) trigger = AvalancheTrigger.U.toString
         if (isBlank(bedSurface)) bedSurface = AvalancheInterface.U.toString
         if (isBlank(modeOfTravel)) modeOfTravel = ModeOfTravel.U.toString
  }
}

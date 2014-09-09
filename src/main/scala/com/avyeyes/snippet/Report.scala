package com.avyeyes.snippet

import scala.xml.XML
import org.apache.commons.lang3.StringUtils.isBlank
import org.squeryl.PrimitiveTypeMode.transaction
import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.JsDialog
import net.liftweb.common.Loggable
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import com.avyeyes.service.ExternalIdService

class Report extends ExternalIdService with Loggable {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  
  var extId = ""; var submitterEmail = ""; var submitterExp = "";
  var lat = ""; var lng = ""; 
  var areaName = ""; var dateStr = ""; var sky = ""; var precip = ""
  var elevation = ""; var aspect = ""; var angle = ""    
  var avyType = ""; var trigger = ""; var bedSurface = ""; var rSize = ""; var dSize = ""
  var caught = ""; var partiallyBuried = ""; var fullyBuried = ""; var injured = ""; var killed = ""
  var modeOfTravel = ""; var comments = ""; var kmlStr = ""
  
  def render = {
    "#avyReportExtId" #> SHtml.hidden(extId = _, extId) &
    "#avyReportLat" #> SHtml.hidden(lat = _, lat) &
    "#avyReportLng" #> SHtml.hidden(lng = _, lng) &
    "#avyReportAreaName" #> SHtml.text(areaName, areaName = _) &
    "#avyReportDate" #> SHtml.text(dateStr, dateStr = _) &
    "#avyReportSky" #> SHtml.hidden(sky = _, sky) &
    "#avyReportPrecip" #> SHtml.hidden(precip = _, precip) &
    "#avyReportElevation" #> SHtml.text(elevation, elevation = _) &
    "#avyReportAspect" #> SHtml.hidden(aspect = _, aspect) &
    "#avyReportAngle" #> SHtml.text(angle, angle = _) &
    "#avyReportType" #> SHtml.hidden(avyType = _, avyType) & 
    "#avyReportTrigger" #> SHtml.hidden(trigger = _, trigger) &
    "#avyReportBedSurface" #> SHtml.hidden(bedSurface = _, bedSurface) &
    "#avyReportRsizeValue" #> SHtml.text(rSize, rSize = _) &
    "#avyReportDsizeValue" #> SHtml.text(dSize, dSize = _) &
    "#avyReportNumCaught" #> SHtml.text(caught, caught = _) &
    "#avyReportNumPartiallyBuried" #> SHtml.text(partiallyBuried, partiallyBuried = _) &
    "#avyReportNumFullyBuried" #> SHtml.text(fullyBuried, fullyBuried = _) &
    "#avyReportNumInjured" #> SHtml.text(injured, injured = _) &
    "#avyReportNumKilled" #> SHtml.text(killed, killed = _) &
    "#avyReportModeOfTravel" #> SHtml.hidden(modeOfTravel = _, modeOfTravel) &
    "#avyReportComments" #> SHtml.textarea(comments, comments = _) &
    "#avyReportSubmitterEmail" #> SHtml.text(submitterEmail, submitterEmail = _) &
    "#avyReportSubmitterExp" #> SHtml.hidden(submitterExp = _, submitterExp) &
    "#avyReportKml" #> SHtml.hidden(kmlStr = _, kmlStr) &
    "#avyReportSubmitBinding" #> SHtml.hidden(doReport)
  }
  
  def doReport(): JsCmd = {
      try {
         checkAutoCompleteValues
         
         val kmlCoordsNode = (XML.loadString(kmlStr) \\ "LinearRing" \ "coordinates").head

         val newAvalanche = Avalanche(extId, false, submitterEmail, ExperienceLevel.withName(submitterExp), 
           strToDblOrZero(lat), strToDblOrZero(lng), areaName, strToDate(dateStr), 
           Sky.withName(sky), Precip.withName(precip), 
           strToIntOrNegOne(elevation), Aspect.withName(aspect), strToIntOrNegOne(angle), 
           AvalancheType.withName(avyType), AvalancheTrigger.withName(trigger), 
           AvalancheInterface.withName(bedSurface), strToDblOrZero(rSize), strToDblOrZero(dSize), 
           strToIntOrNegOne(caught), strToIntOrNegOne(partiallyBuried), strToIntOrNegOne(fullyBuried), 
           strToIntOrNegOne(injured), strToIntOrNegOne(killed), 
           ModeOfTravel.withName(modeOfTravel), comments, kmlCoordsNode.text.trim)

        transaction {
          dao.insertAvalanche(newAvalanche)
	      }
	      
	      logger.info("Avalanche " + extId + " successfully inserted")
        JsDialog.info("avyReportSuccess", getProp("base.url") + extId)
      } catch {
        case e: Exception => {
          logger.error("Error creating avalanche " + extId, e)
          JsDialog.error("avyReportError", e.getMessage())
        }
      } finally {
        unreserveExtId(extId)
      }
  }
  
  private def checkAutoCompleteValues() = {
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

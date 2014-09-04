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
  var extId = ""; var submitterEmail = ""; var submitterExp = "";
  var lat = ""; var lng = ""; 
  var areaName = ""; var dateStr = ""; var sky = ""; var precip = ""
  var elevation = ""; var aspect = ""; var angle = ""    
  var avyType = ""; var trigger = ""; var bedSurface = ""; var rSize = ""; var dSize = ""
  var caught = ""; var partiallyBuried = ""; var fullyBuried = ""; var injured = ""; var killed = ""
  var modeOfTravel = ""; var comments = ""; var kmlStr = ""
  
  val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
    
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
    "#avyReportKml" #> SHtml.hidden(kmlStr = _, "") &
    "#avyReportSubmitBinding" #> SHtml.hidden(doReport)
  }
  
  private def doReport(): JsCmd = {
      try {
         checkAutoCompleteValues
         
         val kmlCoordsNode = (XML.loadString(kmlStr) \\ "LinearRing" \ "coordinates").head

         val newAvalanche = new Avalanche(extId, false, 
           submitterEmail, ExperienceLevel.withName(submitterExp), 
           asDouble(lat) openOr 0, asDouble(lng) openOr 0, 
           areaName, parseDateStr(dateStr), Sky.withName(sky), Precip.withName(precip), 
           asInt(elevation) openOr -1, Aspect.withName(aspect), asInt(angle) openOr -1, 
           AvalancheType.withName(avyType), AvalancheTrigger.withName(trigger), 
           AvalancheInterface.withName(bedSurface), asDouble(rSize) openOr 0, asDouble(dSize) openOr 0, 
           asInt(caught) openOr -1, asInt(partiallyBuried) openOr -1, asInt(fullyBuried) openOr -1, 
           asInt(injured) openOr -1, asInt(killed) openOr -1, 
           ModeOfTravel.withName(modeOfTravel), Some(comments), kmlCoordsNode.text.trim)

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

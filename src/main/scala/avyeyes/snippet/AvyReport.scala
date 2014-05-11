package avyeyes.snippet

import avyeyes.model.Avalanche
import avyeyes.model.AvalancheDb
import avyeyes.model.enums._
import avyeyes.util.AEHelpers._
import avyeyes.util.ui.JsDialog
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import org.squeryl.PrimitiveTypeMode._
import java.text.SimpleDateFormat
import java.util.Date
import java.sql.Timestamp
import scala.xml.XML
import avyeyes.util.ui.JsDialog

object AvyReport {
  var lat = ""; var lng = ""; var areaName = ""
  var dateStr = ""; var sky = ""; var precip = ""
  var elevation = ""; var aspect = ""; var angle = ""    
  var avyType = ""; var trigger = ""; var bedSurface = ""; var rSize = ""; var dSize = ""
  var caught = ""; var partiallyBuried = ""; var fullyBuried = ""; var injured = ""; var killed = ""
  var modeOfTravel = ""; var submitterEmail = ""; var comments = ""; var kmlStr = ""
      
  def render = {
//    S.appendJs(
//        Call("avyEyesView.wireAutoCompletes", ".avySkyAutoComplete", Sky.toJsArray).cmd &
//        Call("avyEyesView.wireAutoCompletes", ".avyPrecipAutoComplete", Precip.toJsArray).cmd &
//        Call("avyEyesView.wireAutoCompletes", ".avyBedSurfaceAutoComplete", AvalancheInterface.toJsArray).cmd &
//        Call("avyEyesView.wireAutoCompletes", ".avySlopeAspectAutoComplete", Aspect.toJsArray).cmd &
//        Call("avyEyesView.wireAutoCompletes", ".avyModeOfTravelAutoComplete", ModeOfTravel.toJsArray).cmd
//        )
    
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
    "#avyReportKml" #> SHtml.hidden(kmlStr = _, "") &
    "#avyReportSubmitBinding" #> SHtml.hidden(doReport)
  }
  
  def doReport() = {
      try {
    	  val kmlCoordsNode = (XML.loadString(kmlStr) \\ "LinearRing" \ "coordinates").head

	      val newAvalanche = new Avalanche(None, false,
	          asDouble(lat) openOr 0, asDouble(lng) openOr 0, areaName,
	          parseDateStr(dateStr), Sky.withCode(sky), Precip.withCode(precip), 
	          asInt(elevation) openOr -1, Aspect.withName(aspect), asInt(angle) openOr -1, 
	    	  AvalancheType.withCode(avyType), AvalancheTrigger.withCode(trigger), 
	    	  AvalancheInterface.withCode(bedSurface), asDouble(rSize) openOr 0, asDouble(dSize) openOr 0, 
	    	  asInt(caught) openOr -1, asInt(partiallyBuried) openOr -1, asInt(fullyBuried) openOr -1, 
	    	  asInt(injured) openOr -1, asInt(killed) openOr -1, 
	    	  ModeOfTravel.withCode(modeOfTravel), Some(comments), None, 
	    	  kmlCoordsNode.text.trim)
    	  
	      transaction {
	    	  AvalancheDb.avalanches.insert(newAvalanche)
	      }
	      
	      JsDialog.info("Avalanche inserted")
      } catch {
        case e: Exception => JsDialog.error("An error occured while processing the avalanche data"
            + " and the avalanche was not saved.", "Exception message: " + e.getMessage())
      }
  }
}

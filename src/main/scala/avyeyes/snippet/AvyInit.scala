package avyeyes.snippet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import avyeyes.model.AvalancheDb._
import avyeyes.model.enums._
import avyeyes.util.AEHelpers._
import avyeyes.util.AEConstants._
import avyeyes.util.ui.KmlCreator
import scala.collection.mutable.ListBuffer


object AvyInit {
    private var extId: Option[String] = None
      
    def render = {
      extId = S.param(EXT_ID_URL_PARAM).toOption
      "#avyInitLiftCallback" #> SHtml.hidden(initJsCalls)
    }
    
    private def initJsCalls(): JsCmd = autoCompleteSourcesCmd & initialFlyToCmd
    
    private def initialFlyToCmd: JsCmd = {
        val initAvalanche = getAvalancheByExtId(extId)
        if (initAvalanche.isDefined) {
            val kml = new KmlCreator().createCompositeKml(initAvalanche.get)
            Call("view.overlaySearchResultKml", kml.toString).cmd &
            Call("view.flyTo", initAvalanche.get.lat, initAvalanche.get.lng, 2000, 70, 
                    getLookAtHeadingForAspect(initAvalanche.get.aspect)).cmd
        } else {
            Call("view.flyTo", INIT_LAT, INIT_LNG, INIT_ALT_METERS, 0, 0).cmd
        }
    }
    
    private def autoCompleteSourcesCmd: JsCmd = {
      JsRaw("""$('.avyTypeAutoComplete').autocomplete('option', 'source', """ + AvalancheType.toJsonArray + """);
               $('.avyTriggerAutoComplete').autocomplete('option', 'source', """ + AvalancheTrigger.toJsonArray + """);
               $('.avySkyAutoComplete').autocomplete('option', 'source', """ + Sky.toJsonArray + """);
               $('.avyPrecipAutoComplete').autocomplete('option', 'source', """ + Precip.toJsonArray + """);
               $('.avyInterfaceAutoComplete').autocomplete('option', 'source', """ + AvalancheInterface.toJsonArray + """);
               $('.avyAspectAutoComplete').autocomplete('option', 'source', """ + Aspect.toJsonArray + """);
               $('.avyModeOfTravelAutoComplete').autocomplete('option', 'source', """ + ModeOfTravel.toJsonArray + """);
            """).cmd
    }
}
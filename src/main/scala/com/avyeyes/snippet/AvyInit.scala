package com.avyeyes.snippet

import com.avyeyes.model.AvalancheDb.getAvalancheByExtId
import com.avyeyes.model.enums._
import com.avyeyes.util.AEConstants.EXT_ID_URL_PARAM
import com.avyeyes.util.AEHelpers.getLookAtHeadingForAspect
import com.avyeyes.util.ui.KmlCreator
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsExp._
import net.liftweb.util.Helpers._


object AvyInit {
    private val INIT_VIEW_LAT = 44
    private val INIT_VIEW_LNG = -115
    private val INIT_VIEW_ALT_METERS = 2700000
    private val INIT_VIEW_CAM_TILT = 0
    private val INIT_VIEW_HEADING = 0
    
    private val INIT_AVY_ALT_METERS = 1300
    private val INIT_AVY_CAM_TILT = 75
    
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
            Call("view.flyTo", initAvalanche.get.lat, initAvalanche.get.lng, INIT_AVY_ALT_METERS, 
                INIT_AVY_CAM_TILT, getLookAtHeadingForAspect(initAvalanche.get.aspect)).cmd
        } else {
            Call("view.flyTo", INIT_VIEW_LAT, INIT_VIEW_LNG, INIT_VIEW_ALT_METERS, 
                INIT_VIEW_CAM_TILT, INIT_VIEW_HEADING).cmd
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
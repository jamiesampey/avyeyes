package com.avyeyes.snippet

import org.squeryl.PrimitiveTypeMode.transaction

import com.avyeyes.model.enums._
import com.avyeyes.persist.SquerylPersistence
import com.avyeyes.service._
import com.avyeyes.util.AEConstants.ExtIdUrlParam
import com.avyeyes.util.AEHelpers.isValidExtId

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsExp._
import net.liftweb.util.Helpers._


class Init extends KmlCreator with PersistenceService with SquerylPersistence {
    private val InitViewLat = 44
    private val InitViewLng = -115
    private val InitViewAltMeters = 2700000
    private val InitViewCamTilt = 0
    private val InitViewHeading = 0
    
    private val InitAvyAltMeters = 1300
    private val InitAvyCamTilt = 75
    
    private var extId: Option[String] = None
      
    def render = {
      extId = S.param(ExtIdUrlParam).toOption
      "#avyInitLiftCallback" #> SHtml.hidden(initJsCalls)
    }
    
    private def initJsCalls(): JsCmd = autoCompleteSourcesCmd & initialFlyToCmd
    
    private def initialFlyToCmd: JsCmd = {
        val initAvalanche = if (isValidExtId(extId)) {
          transaction {
            findViewableAvalanche(extId.get)
          }
        } else None
        
        if (initAvalanche.isDefined) {
          logger.debug("Initial page view with init avy " + extId)
          Call("avyeyes.overlaySearchResultKml", createCompositeKml(initAvalanche.get).toString).cmd &
          Call("avyeyes.flyTo", initAvalanche.get.lat, initAvalanche.get.lng, InitAvyAltMeters, 
              InitAvyCamTilt, getLookAtHeadingForAspect(initAvalanche.get.aspect)).cmd
        } else {
            logger.debug("Initial page view without an init avy")
            Call("avyeyes.flyTo", InitViewLat, InitViewLng, InitViewAltMeters, 
                InitViewCamTilt, InitViewHeading).cmd
        }
    }
    
    private def autoCompleteSourcesCmd: JsCmd = {
      JsRaw(s"$$('.avyTypeAutoComplete').autocomplete('option', 'source', ${AvalancheType.toJsonArray});"
        + s"$$('.avyTriggerAutoComplete').autocomplete('option', 'source', ${AvalancheTrigger.toJsonArray});"
        + s"$$('.avySkyAutoComplete').autocomplete('option', 'source', ${Sky.toJsonArray});"
        + s"$$('.avyPrecipAutoComplete').autocomplete('option', 'source', ${Precip.toJsonArray});"
        + s"$$('.avyInterfaceAutoComplete').autocomplete('option', 'source', ${AvalancheInterface.toJsonArray});"
        + s"$$('.avyAspectAutoComplete').autocomplete('option', 'source', ${Aspect.toJsonArray});"
        + s"$$('.avyModeOfTravelAutoComplete').autocomplete('option', 'source', ${ModeOfTravel.toJsonArray});"
        + s"$$('.avyExperienceLevelAutoComplete').autocomplete('option', 'source', ${ExperienceLevel.toJsonArray});").cmd
    }
    
    private def getLookAtHeadingForAspect(aspect: Aspect.Value): Int = aspect match {
      case Aspect.N => 180
      case Aspect.NE => 225
      case Aspect.E => 270
      case Aspect.SE => 315
      case Aspect.S => 0
      case Aspect.SW => 45
      case Aspect.W => 90
      case Aspect.NW => 135
    }
}
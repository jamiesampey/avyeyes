package com.avyeyes.snippet

import com.avyeyes.data.DaoInjector
import com.avyeyes.model.enums._
import com.avyeyes.service.KmlCreator
import com.avyeyes.util.Constants.ExtIdUrlParam
import com.avyeyes.util.Helpers._
import com.avyeyes.util.JsDialog
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsExp._
import net.liftweb.util.Helpers._


class Init extends KmlCreator with Loggable {
  lazy val dao = DaoInjector.dao.vend
    
  val InitAvyMsgDelayMillis = 5000

  private var extId: Option[String] = None
  
  def render = {
    extId = S.param(ExtIdUrlParam).toOption
    "#avyInitLiftCallback" #> SHtml.hidden(initJsCalls)
  }
  
  def initJsCalls(): JsCmd = autoCompleteSourcesCmd & initialFlyToCmd
  
  private def initialFlyToCmd: JsCmd = {
    val initAvalanche = isValidExtId(extId) match {
      case true => dao.getAvalanche(extId.get)
      case false => None
    }
    
    initAvalanche match {
      case Some(avalanche) => {
        logger.debug("Initial page view with init avy " + extId)

        Call("avyEyesView.addAvalancheAndFlyTo", avalanche.toSearchResultJson).cmd &
          JsDialog.delayedInfo(InitAvyMsgDelayMillis, "initAvalancheFound", dateToStr(avalanche.date),
            avalanche.areaName, ExperienceLevel.getLabel(avalanche.submitterExp))
      }
      case None => {
        logger.debug("Initial page view without an init avy")

        Call("avyEyesView.geolocateAndFlyTo").cmd
      }
    }
  }
  
  private def autoCompleteSourcesCmd: JsCmd = {
    JsRaw(s"$$('.avyTypeAutoComplete').autocomplete('option', 'source', ${AvalancheType.toAutoCompleteSourceJson});"
      + s"$$('.avyTriggerAutoComplete').autocomplete('option', 'source', ${AvalancheTrigger.toAutoCompleteSourceJson});"
      + s"$$('.avySkyAutoComplete').autocomplete('option', 'source', ${SkyCoverage.toAutoCompleteSourceJson});"
      + s"$$('.avyPrecipAutoComplete').autocomplete('option', 'source', ${Precipitation.toAutoCompleteSourceJson});"
      + s"$$('.avyInterfaceAutoComplete').autocomplete('option', 'source', ${AvalancheInterface.toAutoCompleteSourceJson});"
      + s"$$('.avyAspectAutoComplete').autocomplete('option', 'source', ${Aspect.toAutoCompleteSourceJson});"
      + s"$$('.avyModeOfTravelAutoComplete').autocomplete('option', 'source', ${ModeOfTravel.toAutoCompleteSourceJson});"
      + s"$$('.avyExperienceLevelAutoComplete').autocomplete('option', 'source', ${ExperienceLevel.toAutoCompleteSourceJson});")
      .cmd
  }
}
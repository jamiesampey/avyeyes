package com.avyeyes.snippet

import com.avyeyes.data.DaoInjector
import com.avyeyes.model.Converters._
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
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.{JsonAST, Printer}
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

        Call("avyEyesView.addAvalancheAndFlyTo", avalanche.toSearchJson).cmd &
          JsDialog.delayedInfo(InitAvyMsgDelayMillis, "initAvalancheFound", dateToStr(avalanche.date),
            avalanche.areaName, S.?(s"enum.ExperienceLevel.${avalanche.submitterExp.toString}"))
      }
      case None => {
        logger.debug("Initial page view without an init avy")

        Call("avyEyesView.geolocateAndFlyTo").cmd
      }
    }
  }
  
  private def autoCompleteSourcesCmd: JsCmd = {
    JsRaw(s"$$('.avyTypeAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(AvalancheType)});"
      + s"$$('.avyTriggerAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(AvalancheTrigger)});"
      + s"$$('.avySkyAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(SkyCoverage)});"
      + s"$$('.avyPrecipAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(Precipitation)});"
      + s"$$('.avyInterfaceAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(AvalancheInterface)});"
      + s"$$('.avyAspectAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(Aspect)});"
      + s"$$('.avyModeOfTravelAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(ModeOfTravel)});"
      + s"$$('.avyExperienceLevelAutoComplete').autocomplete('option', 'source', ${toAutoCompleteJson(ExperienceLevel)});")
      .cmd
  }

  private def toAutoCompleteJson(enum: AutocompleteEnum): String = {
    Printer.compact(JsonAST.render(JArray(enum.values.toList.map(enumToJson))))
  }
}
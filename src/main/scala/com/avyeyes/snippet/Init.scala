package com.avyeyes.snippet

import com.avyeyes.model.enums._
import com.avyeyes.persist.AvyEyesSqueryl.transaction
import com.avyeyes.persist.DaoInjector
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
  lazy val dao = DaoInjector.avalancheDao.vend
    
  val InitViewAltMeters = 2700000
  val InitViewCamTilt = -90
  val InitViewSearchFormDelayMillis = 3500

  val InitAvyMsgDelayMillis = 5000

  private var extId: Option[String] = None
  
  def render = {
    extId = S.param(ExtIdUrlParam).toOption
    "#avyInitLiftCallback" #> SHtml.hidden(initJsCalls)
  }
  
  def initJsCalls(): JsCmd = autoCompleteSourcesCmd & initialFlyToCmd
  
  private def initialFlyToCmd: JsCmd = {
    val initAvalanche = isValidExtId(extId) match {
      case true => transaction {dao.selectAvalanche(extId.get)}
      case false => None
    }
    
    if (initAvalanche.isDefined) {
      logger.debug("Initial page view with init avy " + extId)
      Call("avyeyes.addAvalancheAndFlyTo", initAvalanche.get.toSearchResultJsonObj).cmd &
      JsDialog.delayedInfo(InitAvyMsgDelayMillis, "initAvalancheFound", dateToStr(initAvalanche.get.avyDate), initAvalanche.get.areaName,
        ExperienceLevel.getLabel(initAvalanche.get.submitterExp))
    } else {
        logger.debug("Initial page view without an init avy")
        Call("avyeyes.geolocateAndFlyTo", InitViewAltMeters, InitViewCamTilt).cmd &
        Call("avyeyes.showSearchDiv", InitViewSearchFormDelayMillis).cmd
    }
  }
  
  private def autoCompleteSourcesCmd: JsCmd = {
    JsRaw(s"$$('.avyTypeAutoComplete').autocomplete('option', 'source', ${AvalancheType.toAutoCompleteSourceJson});"
      + s"$$('.avyTriggerAutoComplete').autocomplete('option', 'source', ${AvalancheTrigger.toAutoCompleteSourceJson});"
      + s"$$('.avySkyAutoComplete').autocomplete('option', 'source', ${Sky.toAutoCompleteSourceJson});"
      + s"$$('.avyPrecipAutoComplete').autocomplete('option', 'source', ${Precip.toAutoCompleteSourceJson});"
      + s"$$('.avyInterfaceAutoComplete').autocomplete('option', 'source', ${AvalancheInterface.toAutoCompleteSourceJson});"
      + s"$$('.avyAspectAutoComplete').autocomplete('option', 'source', ${Aspect.toAutoCompleteSourceJson});"
      + s"$$('.avyModeOfTravelAutoComplete').autocomplete('option', 'source', ${ModeOfTravel.toAutoCompleteSourceJson});"
      + s"$$('.avyExperienceLevelAutoComplete').autocomplete('option', 'source', ${ExperienceLevel.toAutoCompleteSourceJson});")
      .cmd
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
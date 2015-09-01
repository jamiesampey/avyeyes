package com.avyeyes.snippet

import com.avyeyes.model.JsonSerializers._
import com.avyeyes.model.enums._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants.ExtIdUrlParam
import com.avyeyes.util.Helpers._
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.json.Serialization.write
import net.liftweb.util.Helpers._
import net.liftweb.util.Props

class Init extends Loggable {
  val dal = Injectors.dal.vend
    
  val InitAvyMsgDelayMillis = 5000

  private var extId: Option[String] = None
  
  def render = {
    extId = S.param(ExtIdUrlParam).toOption
    "#avyInitLiftCallback" #> SHtml.hidden(initJsCalls)
  }
  
  def initJsCalls(): JsCmd = autoCompleteSourcesCmd & s3ImageBucketCmd & initialFlyToCmd
  
  private def initialFlyToCmd: JsCmd = {
    val initAvalanche = isValidExtId(extId) match {
      case true => dal.getAvalanche(extId.get)
      case false => None
    }
    
    initAvalanche match {
      case Some(a) => {
        logger.debug("Initial page view with init avy " + extId)
        Call("avyEyesView.addAvalancheAndFlyTo", avalancheInitView(a)).cmd
      }
      case None => {
        logger.debug("Initial page view without an init avy")
        Call("avyEyesView.geolocateAndFlyTo").cmd
      }
    }
  }
  
  private def autoCompleteSourcesCmd: JsCmd = {
    JsRaw(s"$$('.avyTypeAutoComplete').autocomplete('option', 'source', ${write(AvalancheType.values)});"
      + s"$$('.avyTriggerAutoComplete').autocomplete('option', 'source', ${write(AvalancheTrigger.values)});"
      + s"$$('.avySkyAutoComplete').autocomplete('option', 'source', ${write(SkyCoverage.values)});"
      + s"$$('.avyPrecipAutoComplete').autocomplete('option', 'source', ${write(Precipitation.values)});"
      + s"$$('.avyInterfaceAutoComplete').autocomplete('option', 'source', ${write(AvalancheInterface.values)});"
      + s"$$('.avyAspectAutoComplete').autocomplete('option', 'source', ${write(Aspect.values)});"
      + s"$$('.avyModeOfTravelAutoComplete').autocomplete('option', 'source', ${write(ModeOfTravel.values)});"
      + s"$$('.avyExperienceLevelAutoComplete').autocomplete('option', 'source', ${write(ExperienceLevel.values)});")
      .cmd
  }

  private def s3ImageBucketCmd: JsCmd = JsRaw(s"$$('#s3ImageBucket').val('${Props.get("s3.imageBucket").openOr("")}');").cmd
}
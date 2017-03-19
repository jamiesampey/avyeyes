package com.avyeyes.snippet

import com.avyeyes.model.JsonSerializers._
import com.avyeyes.model.enums._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants.ExtIdUrlParam
import com.avyeyes.util.Validators.isValidExtId
import net.liftweb.common.{Box, Full, Loggable}
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.json.Serialization.write
import net.liftweb.util.Helpers._

class Init extends Loggable {
  private val R = Injectors.resources.vend
  private val dal = Injectors.dal.vend
  private val user = Injectors.user.vend

  val InitAvyMsgDelayMillis = 5000

  def render = {
    "#avyInitLiftCallback" #> SHtml.hidden(() =>
      initialJsCmds(S.originalRequest.flatMap(_.param(ExtIdUrlParam))))
  }

  private[snippet] def initialJsCmds(extIdBox: Box[String]) =
    autoCompleteSourcesCmd & s3ImageBucketCmd & initialFlyToCmd(extIdBox)

  private def initialFlyToCmd(extIdBox: Box[String]): JsCmd = extIdBox match {
    case Full(extId) if isValidExtId(extId) => dal.getAvalanche(extId).flatMap { avalanche =>
      if (user.isAuthorizedToViewAvalanche(avalanche)) Some(avalanche) else None } match {
        case Some(a) =>
        logger.debug(s"Initial page view with init avalanche ${a.extId}")
        Call("avyEyesView.addAvalancheAndFlyTo", avalancheInitViewData(a)).cmd
      case None =>
        logger.debug("Initial page view without an init avalanche")
        Call("avyEyesView.geolocateAndFlyTo").cmd
      }
      case _ => Call("avyEyesView.geolocateAndFlyTo").cmd
  }

  private def autoCompleteSourcesCmd: JsCmd = {
    JsRaw(s"$$('.avyTypeAutoComplete').avycomplete('option', 'source', ${write(AvalancheType.selectableValues)});"
      + s"$$('.avyTriggerAutoComplete').avycomplete('option', 'source', ${write(AvalancheTrigger.selectableValues)});"
      + s"$$('.avyTriggerModifierAutoComplete').avycomplete('option', 'source', ${write(AvalancheTriggerModifier.selectableValues)});"
      + s"$$('.avyInterfaceAutoComplete').avycomplete('option', 'source', ${write(AvalancheInterface.selectableValues)});"
      + s"$$('.avyDirectionAutoComplete').avycomplete('option', 'source', ${write(Direction.selectableValues)});"
      + s"$$('.avyWindSpeedAutoComplete').avycomplete('option', 'source', ${write(WindSpeed.selectableValues)});"
      + s"$$('.avyModeOfTravelAutoComplete').avycomplete('option', 'source', ${write(ModeOfTravel.selectableValues)});"
      + s"$$('.avyExperienceLevelAutoComplete').avycomplete('option', 'source', ${write(ExperienceLevel.selectableValues)});")
      .cmd
  }

  private def s3ImageBucketCmd: JsCmd = JsRaw(s"$$('#s3Bucket').val('${R.getProperty("s3.bucket")}');").cmd
}
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
  val R = Injectors.resources.vend
  val dal = Injectors.dal.vend

  val InitAvyMsgDelayMillis = 5000

  def render = {
    "#avyInitLiftCallback" #> SHtml.hidden(() =>
      initialJsCmds(S.originalRequest.flatMap(_.param(ExtIdUrlParam))))
  }

  private[snippet] def initialJsCmds(extIdBox: Box[String]) =
    autoCompleteSourcesCmd & s3ImageBucketCmd & initialFlyToCmd(extIdBox)

  private def initialFlyToCmd(extIdBox: Box[String]): JsCmd = {
    val initAvalanche = extIdBox match {
      case Full(extId) if isValidExtId(extId) => dal.getAvalanche(extId)
      case _ => None
    }

    initAvalanche match {
      case Some(a) => {
        logger.debug(s"Initial page view with init avalanche ${a.extId}")
        Call("avyEyesView.addAvalancheAndFlyTo", avalancheInitView(a)).cmd
      }
      case None => {
        logger.debug("Initial page view without an init avalanche")
        Call("avyEyesView.geolocateAndFlyTo").cmd
      }
    }
  }

  private def autoCompleteSourcesCmd: JsCmd = {
    JsRaw(s"$$('.avyTypeAutoComplete').avycomplete('option', 'source', ${write(AvalancheType.values)});"
      + s"$$('.avyTriggerAutoComplete').avycomplete('option', 'source', ${write(AvalancheTrigger.values)});"
      + s"$$('.avyTriggerCauseAutoComplete').avycomplete('option', 'source', ${write(AvalancheTriggerCause.values)});"
      + s"$$('.avyInterfaceAutoComplete').avycomplete('option', 'source', ${write(AvalancheInterface.values)});"
      + s"$$('.avyDirectionAutoComplete').avycomplete('option', 'source', ${write(Direction.values)});"
      + s"$$('.avyWindSpeedAutoComplete').avycomplete('option', 'source', ${write(WindSpeed.values)});"
      + s"$$('.avyModeOfTravelAutoComplete').avycomplete('option', 'source', ${write(ModeOfTravel.values)});"
      + s"$$('.avyExperienceLevelAutoComplete').avycomplete('option', 'source', ${write(ExperienceLevel.values)});")
      .cmd
  }

  private def s3ImageBucketCmd: JsCmd = JsRaw(s"$$('#s3ImageBucket').val('${R.getProperty("s3.imageBucket")}');").cmd
}
package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.{Avalanche, JsonSerializers}
import com.avyeyes.util.Constants.AvalancheEditWindow
import org.joda.time.{DateTime, Seconds}
import org.json4s.Formats
import org.json4s.jackson.Serialization.write
import play.api.Logger
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Singleton
class AvalancheController @Inject()(dal: CachedDAL, jsonSerializers: JsonSerializers, logger: Logger) extends Controller {
  import jsonSerializers._

  implicit val formats: Formats = jsonSerializers.formats
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def find(extId: String) = Action { implicit request =>
    logger.debug(s"finding avalanche $extId")
    dal.getAvalanche(extId) match {
      case Some(avalanche) => Ok(write(avalancheInitViewData(avalanche)))
      case _ => NotFound
    }
  }

  def details(extId: String, editKeyOpt: Option[String]) = Action.async { implicit request =>
    logger.debug(s"serving avalanche details $extId")

    for {
      avalancheOption <- dal.getAvalancheFromDisk(extId)
      images <- dal.getAvalancheImages(extId)
    } yield {
      avalancheOption match {
        case Some(avalanche) if editAllowed(avalanche, editKeyOpt) => Ok(write(avalancheReadWriteData(avalanche, images)))
        case Some(avalanche) => Ok(write(avalancheReadOnlyData(avalanche, images)))
        case _ => NotFound
      }
    }
  }

  private def editAllowed(avalanche: Avalanche, editKeyOpt: Option[String]) = editKeyOpt match {
    case Some(editKey) if editKey.toLong == avalanche.editKey =>
      Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
    case _ => false
  }
}

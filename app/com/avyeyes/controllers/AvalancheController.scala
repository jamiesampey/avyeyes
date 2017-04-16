package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.JsonSerializers
import org.json4s.Formats
import org.json4s.jackson.Serialization.write
import play.api.Logger
import play.api.mvc.{Action, Controller}

@Singleton
class AvalancheController @Inject()(dal: CachedDAL, jsonSerializers: JsonSerializers, logger: Logger) extends Controller {

  implicit val formats: Formats = jsonSerializers.formats

  def find(extId: String) = Action { implicit request =>
    logger.debug(s"serving avalanche $extId")
    dal.getAvalanche(extId) match {
      case Some(avalanche) => Ok(write(jsonSerializers.avalancheInitViewData(avalanche)))
      case _ => NotFound
    }
  }
}

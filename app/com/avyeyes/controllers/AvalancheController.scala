package com.avyeyes.controllers

import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.JsonSerializers
import org.json4s.Formats
import play.api.Logger
import play.api.mvc.{Action, Controller}

@Singleton
class AvalancheController @Inject()(dalFactory: CachedDAL, jsonSerializers: JsonSerializers, logger: Logger) extends Controller {

  implicit val formats: Formats = jsonSerializers.formats

  def find(extId: String) = Action { implicit request =>
    logger.debug(s"finding avalanche $extId")
    //    val avalancheOpt = dal.getAvalanche(extId)
    //
    //    render {
    //      case _ if avalancheOpt.isDefined => Status(200)(write(jsonSerializers.avalancheInitViewData(avalancheOpt.get)))
    //      case _ => Status(400)
    //    }
    BadRequest
  }
}

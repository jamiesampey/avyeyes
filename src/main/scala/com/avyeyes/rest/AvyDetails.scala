package com.avyeyes.rest

import com.avyeyes.model._
import com.avyeyes.service.UserInjector
import net.liftweb.common.Loggable
import net.liftweb.http.{JsonResponse, NotFoundResponse}
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._


class AvyDetails extends RestHelper with Loggable {
  lazy val dao = DaoInjector.avalancheDao.vend
  lazy val userSession = UserInjector.userSession.vend

  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => {
      val avyJsonOption = dao.selectAvalanche(extId) match {
        case Some(avalanche) => {
          userSession.isAuthorizedSession match {
            case true => Some(getJson(avalanche) ~ getJsonAdminFields(avalanche))
            case false => Some(getJson(avalanche))
          }
        }
        case None => None
      }

      avyJsonOption match {
        case Some(json) => {
          logger.debug("Serving details for avy " + extId)
          JsonResponse(json)
        }
        case None => {
          logger.warn("Avy details request failed. Could not serve details for avy " + extId)
          NotFoundResponse("Avalanche not found")
        }
      }
    }
  }

  private def getJson(a: Avalanche) = {
    val imagesMetadata = dao.selectAvalancheImagesMetadata(a.extId)
    a.toJson ~ ("images" -> JArray(
      imagesMetadata map Function.tupled ((f,m,s) => imageMetadataToJObject(f,m,s))))
  }
  
  private def imageMetadataToJObject(filename: String, mimeType: String, size: Int): JObject = JObject(List(
    JField("filename", JString(filename)),
    JField("mimeType", JString(mimeType)),
    JField("size", JInt(size)) 
  ))
  
  private def getJsonAdminFields(a: Avalanche) = {
    ("viewable", a.viewable) ~ ("submitterEmail", a.getSubmitter.email)
  }
}
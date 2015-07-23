package com.avyeyes.rest

import com.avyeyes.data.DaoInjector
import com.avyeyes.model._
import com.avyeyes.service.UserInjector
import net.liftweb.common.Loggable
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{JsonResponse, NotFoundResponse}
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._


class AvyDetails extends RestHelper with Loggable {
  lazy val dao = DaoInjector.dao.vend
  lazy val userSession = UserInjector.userSession.vend

  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => {
      val avyJsonOption = dao.getAvalancheFromDisk(extId) match {
        case Some(a) => {
          val images = dao.getAvalancheImages(a.extId)
          userSession.isAuthorizedSession match {
            case true => Some(a.toAdminDetailsJson(images))
            case false => Some(a.toDetailsJson(images))
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

}
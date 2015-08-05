package com.avyeyes.rest

import com.avyeyes.data.DalInjector
import com.avyeyes.model.JsonSerializers._
import com.avyeyes.service.UserInjector
import net.liftweb.common.Loggable
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{JsonResponse, NotFoundResponse}


class AvyDetails extends RestHelper with Loggable {
  lazy val dal = DalInjector.dal.vend
  lazy val userSession = UserInjector.userSession.vend

  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => {
      val avyJsonOption = dal.getAvalancheFromDisk(extId) match {
        case Some(a) => {
          val images = dal.getAvalancheImages(a.extId)
          userSession.isAuthorizedSession match {
            case true => Some(avalancheAdminDetails(a, images))
            case false => Some(avalancheDetails(a, images))
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
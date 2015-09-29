package com.avyeyes.rest

import com.avyeyes.model.JsonSerializers._
import com.avyeyes.service.Injectors
import net.liftweb.common.Loggable
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{JsonResponse, NotFoundResponse}


class AvyDetails extends RestHelper with Loggable {
  lazy val dal = Injectors.dal.vend
  lazy val userSession = Injectors.user.vend

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
          logger.debug(s"Serving details for avalanche $extId")
          JsonResponse(json)
        }
        case None => {
          logger.warn(s"Avy details request failed. Could not serve details for avalanche $extId")
          NotFoundResponse("Avalanche not found")
        }
      }
    }
  }

}
package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.model.JsonSerializers._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants.AvalancheEditWindow
import net.liftweb.common.{Full, Box, Loggable}
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{S, JsonResponse, NotFoundResponse}
import org.joda.time.{Seconds, DateTime}

class AvyDetails extends RestHelper with Loggable {
  lazy val dal = Injectors.dal.vend
  lazy val userSession = Injectors.user.vend

  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => {
      val avyJsonOption = dal.getAvalancheFromDisk(extId) match {
        case Some(a) => {
          val images = dal.getAvalancheImages(a.extId)
          if (userSession.isAuthorizedSession || withinEditWindow(a, S.param("edit")))
            Some(avalancheEditDetails(a, images))
          else
            Some(avalancheDetails(a, images))
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

  private def withinEditWindow(avalanche: Avalanche, editKeyBox: Box[String]): Boolean = editKeyBox match {
    case Full(editKey) if editKey.toLong == (avalanche.createTime.getMillis / 1000) =>
      Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
    case _ => false
  }
}
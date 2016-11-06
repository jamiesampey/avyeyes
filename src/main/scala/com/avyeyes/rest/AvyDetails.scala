package com.avyeyes.rest

import com.avyeyes.model.Avalanche
import com.avyeyes.model.JsonSerializers._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants.AvalancheEditWindow
import com.avyeyes.util.FutureOps._
import net.liftweb.common.{Box, Full, Loggable}
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{JsonResponse, NotFoundResponse, S}
import org.joda.time.{DateTime, Seconds}

import scala.concurrent.ExecutionContext

class AvyDetails extends RestHelper with Loggable {
  lazy val dal = Injectors.dal.vend
  lazy val userSession = Injectors.user.vend

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => {
      val tupleFuture = for {
        avalancheOption <- dal.getAvalancheFromDisk(extId)
        images <- dal.getAvalancheImages(extId)
      } yield (avalancheOption, images)

      tupleFuture.resolve match {
        case (Some(a), images) if userSession.isAuthorizedSession || withinEditWindow(a, S.param("edit")) =>
          JsonResponse(avalancheReadWriteData(a, images))
        case (Some(a), images) =>
          JsonResponse(avalancheReadOnlyData(a, images))
        case (None, _) =>
          logger.warn(s"Avy details request failed. Could not serve details for avalanche $extId")
          NotFoundResponse("Avalanche not found")
      }
    }
  }

  private def withinEditWindow(avalanche: Avalanche, editKeyBox: Box[String]): Boolean = editKeyBox match {
    case Full(editKey) if editKey.toLong == avalanche.editKey =>
      Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
    case _ => false
  }
}
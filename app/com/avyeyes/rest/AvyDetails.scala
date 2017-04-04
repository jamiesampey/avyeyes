package com.avyeyes.rest

import com.avyeyes.model.JsonSerializers._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants._
import com.avyeyes.util.FutureOps._
import net.liftweb.common.Loggable
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{JsonResponse, NotFoundResponse, S}

import scala.concurrent.ExecutionContext

class AvyDetails extends RestHelper with Loggable {
  lazy val dal = Injectors.dal.vend
  lazy val user = Injectors.user.vend

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => {
      val tupleFuture = for {
        avalancheOption <- dal.getAvalancheFromDisk(extId)
        images <- dal.getAvalancheImages(extId)
      } yield (avalancheOption, images)

      tupleFuture.resolve match {
        case (Some(a), images) if user.isAuthorizedToEditAvalanche(a, S.param(EditParam)) =>
          logger.debug(s"Serving read-write avy details for avalanche $extId")
          JsonResponse(avalancheReadWriteData(a, images))
        case (Some(a), images) if user.isAuthorizedToViewAvalanche(a) =>
          logger.debug(s"Serving read-only avy details for avalanche $extId")
          JsonResponse(avalancheReadOnlyData(a, images))
        case _ =>
          logger.warn(s"Avy details request failed. Could not serve details for avalanche $extId")
          NotFoundResponse("Avalanche not found")
      }
    }
  }
}
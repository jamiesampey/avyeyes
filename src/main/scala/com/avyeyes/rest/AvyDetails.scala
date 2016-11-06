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

import scala.concurrent.{ExecutionContext, Future}

class AvyDetails extends RestHelper with Loggable {
  lazy val dal = Injectors.dal.vend
  lazy val userSession = Injectors.user.vend

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => dal.getAvalancheFromDisk(extId).flatMap {
      case Some(a) if userSession.isAuthorizedSession || withinEditWindow(a, S.param("edit")) => dal.getAvalancheImages(a.extId).map(images => avalancheReadWriteData(a, images)).map(JsonResponse(_))
      case Some(a) => dal.getAvalancheImages(a.extId).map(images => avalancheReadOnlyData(a, images)).map(JsonResponse(_))
      case None =>
        logger.warn(s"Avy details request failed. Could not serve details for avalanche $extId")
        Future { NotFoundResponse("Avalanche not found") }
    }.resolve
  }

  private def withinEditWindow(avalanche: Avalanche, editKeyBox: Box[String]): Boolean = editKeyBox match {
    case Full(editKey) if editKey.toLong == avalanche.editKey =>
      Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
    case _ => false
  }
}
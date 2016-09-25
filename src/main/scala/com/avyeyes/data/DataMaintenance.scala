package com.avyeyes.data

import akka.actor._
import com.avyeyes.service.Injectors
import com.avyeyes.util.FutureOps._
import net.liftweb.common.Loggable

import scala.concurrent.ExecutionContext

class DataMaintenance extends Actor with Loggable {
  val dal = Injectors.dal.vend
  val s3 = Injectors.s3.vend

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  def receive = {
    case DataMaintenance.run => performMaintenance
    case _ => logger.error("Received unknown message")
  }

  private def performMaintenance = {
    logger.info("RUNNING DATA MAINTENANCE")

    logger.info("Refreshing in-memory avalanche cache")
    dal.getAvalanchesFromDisk.map { avalanchesFromDisk =>
      AllAvalanchesMap.clear
      AllAvalanchesMap ++= avalanchesFromDisk.map(a => a.extId -> a.copy(comments = None)).toMap
    }.resolve
    logger.info(s"Refreshed avalanche cache with ${AllAvalanchesMap.size} avalanches")

    logger.info("Pruning orphan images")
    dal.deleteOrphanAvalancheImages.map { _.foreach(s3.deleteAllImages) }.resolve
    logger.info(s"Pruned orphan images from database and S3")

    logger.info("DATA MAINTENANCE COMPLETE")
  }
}

object DataMaintenance {
  val run = "run"
}
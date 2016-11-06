package com.avyeyes.data

import akka.actor._
import com.avyeyes.service.Injectors
import net.liftweb.common.Loggable

import scala.concurrent.ExecutionContext

class DataMaintenance extends Actor with Loggable {
  val dal = Injectors.dal.vend
  val s3 = Injectors.s3.vend

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  def receive = {
    case DataMaintenance.run =>
      logger.info("RUNNING DATA MAINTENANCE")

      dal.getAvalanchesFromDisk.map { avalanchesFromDisk =>
        AllAvalanchesMap.clear
        AllAvalanchesMap ++= avalanchesFromDisk.map(a => a.extId -> a.copy(comments = None)).toMap
        logger.info(s"Refreshed avalanche cache with ${AllAvalanchesMap.size} avalanches")
      }

      dal.deleteOrphanAvalancheImages.map { unfinishedReports =>
        unfinishedReports.foreach(s3.deleteAllImages)
        logger.info(s"Pruned orphan images from database and S3 for ${unfinishedReports.size} unfinished reports")
      }

    case _ => logger.error("Received unknown message")
  }
}

object DataMaintenance {
  val run = "run"
}
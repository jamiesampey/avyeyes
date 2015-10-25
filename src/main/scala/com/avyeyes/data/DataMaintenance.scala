package com.avyeyes.data

import akka.actor._
import com.avyeyes.service.{Injectors, ExternalIdService}
import net.liftweb.common.Loggable

class DataMaintenance extends Actor with ExternalIdService with Loggable {
  val dal = Injectors.dal.vend
  val s3 = Injectors.s3.vend

  def receive = {
    case DataMaintenance.run => performMaintenance
    case _ => logger.error("Received unknown message")
  }

  private def performMaintenance = {
    logger.info("RUNNING DATA MAINTENANCE")

    logger.info("Refreshing in-memory avalanche cache")
    AllAvalanchesMap.clear
    AllAvalanchesMap ++= dal.getAvalanchesFromDisk.map(a =>
      (a.extId, a.copy(comments = None)))
    logger.info(s"Refreshed avalanche cache with ${AllAvalanchesMap.size} avalanches")

    logger.info("Pruning orphan images")
    val prunedExtIds = pruneImages
    prunedExtIds.foreach(s3.deleteAllImages)
    logger.info(s"Pruned orphan images from database and S3")

    logger.info("DATA MAINTENANCE COMPLETE")
  }

  private def pruneImages: Seq[String] = {
    val orphanImages = dal.getOrphanAvalancheImages.filter(img => !reservationExists(img.avalanche))
    val unfinishedReports = orphanImages.map(_.avalanche).distinct

    logger.info(s"Pruning ${orphanImages.size} orphan images from ${unfinishedReports.size} unfinished avalanche reports")
    orphanImages.foreach(img => dal.deleteAvalancheImage(img.avalanche, img.filename))

    unfinishedReports
  }
}

object DataMaintenance {
  val run = "run"
}
package com.avyeyes.data

import akka.actor._
import com.avyeyes.data.DatabaseSchema._
import com.avyeyes.service.{AmazonS3ImageService, ExternalIdService}
import net.liftweb.common.Loggable
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object DatabaseMaintenance {
  val run = "run"
}

class DatabaseMaintenance extends Actor with ExternalIdService with Loggable {
  val db = Database.forDataSource(postgresDataSource)
  val s3 = new AmazonS3ImageService

  def receive = {
    case DatabaseMaintenance.run => {
      logger.info("Refreshing in-memory avalanche cache")
      AllAvalanchesMap.clear
      AllAvalanchesMap ++= Await.result(db.run(Avalanches.result), Duration.Inf).map(a =>
        (a.extId, a.copy(comments = None)))
      logger.info(s"Refreshed avalanche cache with ${AllAvalanchesMap.size} avalanches")

      logger.info("Pruning orphan images")
      val extIdsForPrune = Await.result(pruneImages, Duration.Inf)
      extIdsForPrune.foreach(s3.deleteAllImages)
      logger.info(s"Pruned orphan images from database and S3")
    }
    case _ => logger.error("Received unknown message")
  }

  private def pruneImages: Future[Seq[String]] = {
    db.run(
      AvalancheImages.filter(img => !Avalanches.filter(_.extId === img.avyExtId).exists).result
    ).flatMap { orphanImagesResult =>
      val imagesForPrune = orphanImagesResult.filter(img => !reservationExists(img.avyExtId))
      val unfinishedReports = imagesForPrune.map(_.avyExtId).distinct
      logger.info(s"Pruning ${imagesForPrune.size} orphan images from ${unfinishedReports.size} unfinished avalanche reports")

      db.run(
        AvalancheImages.filter(img => img.avyExtId inSetBind unfinishedReports).delete
      ).flatMap { _ =>
        Future { unfinishedReports }
      }
    }
  }

}

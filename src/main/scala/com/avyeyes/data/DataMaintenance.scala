package com.avyeyes.data

import javax.sql.DataSource

import akka.actor._
import com.avyeyes.service.{Injectors, ExternalIdService}
import net.liftweb.common.Loggable
import slick.driver.{PostgresDriver, JdbcProfile}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DataMaintenance(val driver: JdbcProfile = PostgresDriver, ds: DataSource = PostgresDataSource)
  extends DatabaseComponent with SlickColumnMappers with DriverComponent
  with Actor with ExternalIdService with Loggable {

  import driver.api._

  val db = Database.forDataSource(ds)
  val s3 = Injectors.s3.vend

  def receive = {
    case DataMaintenance.run => performMaintenance
    case _ => logger.error("Received unknown message")
  }

  private def performMaintenance = {
    logger.info("RUNNING DATA MAINTENANCE")

    logger.info("Refreshing in-memory avalanche cache")
    AllAvalanchesMap.clear
    AllAvalanchesMap ++= Await.result(db.run(Avalanches.result), Duration.Inf).map(a =>
      (a.extId, a.copy(comments = None)))
    logger.info(s"Refreshed avalanche cache with ${AllAvalanchesMap.size} avalanches")

    logger.info("Pruning orphan images")
    val extIdsForPrune = Await.result(pruneImages, Duration.Inf)
    extIdsForPrune.foreach(s3.deleteAllImages)
    logger.info(s"Pruned orphan images from database and S3")

    logger.info("DATA MAINTENANCE COMPLETE")
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

object DataMaintenance {
  val run = "run"
}
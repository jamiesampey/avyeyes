package com.avyeyes.data

import akka.actor._
import com.avyeyes.data.DatabaseSchema._
import com.avyeyes.model.Avalanche
import com.avyeyes.service.AmazonS3ImageService
import net.liftweb.common.Loggable
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object DatabaseMaintenance {
  val run = "run"
}

class DatabaseMaintenance extends Actor with Loggable {
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
      logger.info(s"Pruned orphan images for ${extIdsForPrune.size} unfinished avalanche reports")
    }
    case _ => logger.error("Received unknown message")
  }

  private def pruneImages: Future[Set[String]] = db.run {
//    val extIdAction = Avalanches.map(_.extId).result
//    val orphanImagesAction = for {
//      img <- AvalancheImages
//      if img !inSetBind extIdAction
//    } yield image

    //    val orphanImageExtIds = from(avalancheImages)(img => where(
    //      img.avyExtId notIn(from(avalanches)(a => select(a.extId)))) select(img.avyExtId)).distinct.toSet
    //
    //    val imageExtIdsForDelete = orphanImageExtIds filter(!reservationExists(_))
    //
    //    if (imageExtIdsForDelete.size > 0) {
    //      val orphanImageCount = from(avalancheImages)(img => where(
    //        img.avyExtId in imageExtIdsForDelete) compute count).toInt
    //
    //      logger.info(s"Pruning $orphanImageCount orphan images for ${imageExtIdsForDelete.size}"
    //        + " unfinished avalanche report(s)")
    //      avalancheImages.deleteWhere(img => img.avyExtId in imageExtIdsForDelete)
    //    } else {
    //      logger.info("No orphan images found for pruning")
    //    }
    //
    //    imageExtIdsForDelete

    ???
  }
}

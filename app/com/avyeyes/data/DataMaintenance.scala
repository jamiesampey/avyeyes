package com.avyeyes.data

import javax.inject._

import akka.actor._
import com.avyeyes.service.{AmazonS3Service, ExternalIdService}
import play.api.Logger

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class DataMaintenance @Inject()(val logger: Logger, dal: CachedDAL, s3: AmazonS3Service) extends Actor with ExternalIdService  {

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  def receive = {
    case DataMaintenance.run =>
      logger.info("RUNNING DATA MAINTENANCE")

      dal.getAvalanchesFromDisk onComplete {
        case Success(avalanchesFromDisk) if avalanchesFromDisk.nonEmpty =>
          AllAvalanchesMap.clear
          AllAvalanchesMap ++= avalanchesFromDisk.map(a => a.extId -> a.copy(comments = None)).toMap
          logger.info(s"Refreshed in-memory cache with ${AllAvalanchesMap.size} avalanches from the DB")

          s3.allAvalancheKeys.map { _.foreach { s3AvalancheKey =>
            val extId = s3AvalancheKey.split("/").last
            logger.debug(s"Checking if S3 avalanche $extId exists in the DB")
            if (!AllAvalanchesMap.contains(extId) && !reservationExists(extId)) {
              logger.info(s"Avalanche $extId is not in the DB. Deleting S3 content for that avalanche")
              s3.deleteAllFiles(extId)
            }
          }}

          dal.deleteOrphanAvalancheImages

        case Failure(ex) => logger.error("Failed to retrieve avalanches from DB. Cannot perform S3 and DB data maintenance", ex)
      }

    case _ => logger.error("Received unknown message")
  }
}

object DataMaintenance {
  val run = "run"
}
package com.avyeyes.service

import akka.actor._
import com.avyeyes.data.DaoInjector
import net.liftweb.common.Loggable

object ImagePruneService {
  val prune = "prune"
}

class ImagePruneService extends Actor with Loggable {
  lazy val dao = DaoInjector.dao.vend
  val s3 = new AmazonS3ImageService

  def receive = {
    case ImagePruneService.prune => {
      logger.info("Pruning orphan images")
      val deletedImageExtIds = dao.pruneImages()

      deletedImageExtIds.foreach(s3.deleteAllImages)
    }
    case _ => logger.error("Received unknown message")
  }
}

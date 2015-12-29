package com.avyeyes.rest

import java.util.UUID

import com.avyeyes.model.AvalancheImage
import com.avyeyes.service.{Injectors, UnauthorizedException}
import com.avyeyes.util.Constants._
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime

class Images extends RestHelper with Loggable {
  val dal = Injectors.dal.vend
  val s3 = Injectors.s3.vend
  val R = Injectors.resources.vend

  serve {
    case "rest" :: "images" :: avyExtId :: Nil Post req => {
      if (dal.countAvalancheImages(avyExtId) >= MaxImagesPerAvalanche) {
        BadResponse()
      } else {
        val fph = req.uploadedFiles(0)
        val newFilename = s"${UUID.randomUUID().toString}.${fph.fileName.split('.').last.toLowerCase}"

        s3.uploadImage(avyExtId, newFilename, fph.mimeType, fph.file)

        dal.insertAvalancheImage(
          AvalancheImage(DateTime.now, avyExtId, newFilename, fph.fileName, fph.mimeType, fph.length.toInt)
        )

        // if adding an image to an existing viewable avalanche, allow the image to be viewed
        for (avalanche <- dal.getAvalanche(avyExtId) if avalanche.viewable) {
          s3.allowPublicImageAccess(avyExtId)
        }

        JsonResponse(
          ("extId" -> avyExtId) ~
          ("filename" -> newFilename) ~
          ("origFilename" -> fph.fileName) ~
          ("size" -> fph.length)
        )
      }
    }

    case "rest" :: "images" :: avyExtId :: baseFilename :: Nil JsonPut json->req => json \ "caption" match {
      case JString(caption) if caption.nonEmpty =>
        dal.updateAvalancheImage(avyExtId, baseFilename, Some(caption))
        OkResponse()
      case JString("") =>
        dal.updateAvalancheImage(avyExtId, baseFilename, None)
        OkResponse()
      case _ =>
        logger.error("Received an image caption PUT request, but the caption payload was missing")
        BadResponse()
    }

    case "rest" :: "images" :: avyExtId :: Nil JsonPut json->req => json \ "order" match {
      case JArray(order) =>
        println(s"image order ARRAY is $order")
        OkResponse()
      case _ =>
        logger.error("Received an image order PUT request, but the order payload was missing")
        BadResponse()
    }

    case "rest" :: "images" :: avyExtId :: baseFilename :: Nil Delete req => {
      try {
        dal.getAvalancheImage(avyExtId, baseFilename).map(_.filename) match {
          case Some(filename) =>
            s3.deleteImage(avyExtId, filename)
            dal.deleteAvalancheImage(avyExtId, filename)
          case _ => logger.error(s"Unable to retrieve image filename for delete. Base filename = $baseFilename")
        }

        OkResponse()
      } catch {
        case ue: UnauthorizedException => UnauthorizedResponse("AvyEyes auth required")
        case e: Exception => InternalServerErrorResponse()
      }
    }
  }
}
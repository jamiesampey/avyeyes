package com.avyeyes.rest

import java.util.UUID

import com.avyeyes.model.AvalancheImage
import com.avyeyes.service.{Injectors, UnauthorizedException}
import com.avyeyes.util.Constants._
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime

class Images extends RestHelper with Loggable {
  val dal = Injectors.dal.vend
  val s3 = Injectors.s3.vend
  val R = Injectors.resources.vend

  serve {
    case "rest" :: "images" :: avyExtId :: Nil Post req => {
      if (dal.countAvalancheImages(avyExtId) >= MaxImagesPerAvalanche) {
        ResponseWithReason(BadResponse(), R.getMessage("rwAvyFormMaxImagesExceeded",
          MaxImagesPerAvalanche).toString)
      } else {
        val fph = req.uploadedFiles(0)
        val newFilename = s"${UUID.randomUUID().toString}.${fph.fileName.split('.').last.toLowerCase}"

        s3.uploadImage(avyExtId, newFilename, fph.mimeType, fph.file)

        dal.insertAvalancheImage(
          AvalancheImage(DateTime.now, avyExtId, newFilename, fph.fileName, fph.mimeType, fph.length.toInt)
        )

        JsonResponse(
          ("extId" -> avyExtId) ~
          ("filename" -> newFilename) ~
          ("origFilename" -> fph.fileName) ~
          ("size" -> fph.length)
        )
      }
    }

    case "rest" :: "images" :: avyExtId :: baseFilename :: Nil Delete req => {
      try {
        dal.getAvalancheImage(avyExtId, baseFilename).map(_.filename) match {
          case Some(filename) =>
            dal.deleteAvalancheImage(avyExtId, filename)
            s3.deleteImage(avyExtId, filename)
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
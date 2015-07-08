package com.avyeyes.rest

import java.util.UUID

import com.avyeyes.data.DaoInjector
import com.avyeyes.model.AvalancheImage
import com.avyeyes.service.AmazonS3ImageService
import com.avyeyes.util.Constants._
import com.avyeyes.util.Helpers._
import com.avyeyes.util.UnauthorizedException
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime

class Images extends RestHelper with Loggable {
  lazy val dao = DaoInjector.diskDao.vend
  val s3 = new AmazonS3ImageService

  serve {
    case "rest" :: "images" :: avyExtId :: Nil Post req => {
      if (dao.countAvalancheImages(avyExtId) >= MaxImagesPerAvalanche) {
        ResponseWithReason(BadResponse(), getMessage("rwAvyFormMaxImagesExceeded",
          MaxImagesPerAvalanche).toString)
      } else {
        val fph = req.uploadedFiles(0)
        val newFilename = s"${UUID.randomUUID().toString}.${fph.fileName.split('.').last.toLowerCase}"

        s3.uploadImage(avyExtId, newFilename, fph.mimeType, fph.file)

        dao.insertAvalancheImage(
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
    
    case "rest" :: "images" :: avyExtId :: fileBaseName :: Nil Delete req => {
      try {
        s3.deleteImage(avyExtId, fileBaseName)
        dao.deleteAvalancheImage(avyExtId, fileBaseName)
        OkResponse()
      } catch {
        case ue: UnauthorizedException => UnauthorizedResponse("Avy Eyes auth required")
        case e: Exception => InternalServerErrorResponse()
      }
    }
  }
}
package com.avyeyes.rest

import java.io.ByteArrayInputStream

import com.avyeyes.model._
import com.avyeyes.persist._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.UnauthorizedException
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonDSL._
import org.squeryl.PrimitiveTypeMode._

object Images extends RestHelper {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend

  serve {
    case "rest" :: "images" :: avyExtId :: filename :: Nil Get req => {
      val returnedImg = transaction { 
        dao.selectAvalancheImage(avyExtId, filename) 
      }
      
      returnedImg match {
        case Some(avyImg) => val byteStream = new ByteArrayInputStream(avyImg.bytes)
          StreamingResponse(byteStream, () => byteStream.close(), avyImg.bytes.length, 
            ("Content-Type", avyImg.mimeType) :: Nil, Nil, 200)
        case _ => NotFoundResponse(s"Image $avyExtId/$filename not found")
      }
    }
    
    case "rest" :: "images" :: avyExtId :: Nil Post req => {
      val response = transaction {
        if (dao.countAvalancheImages(avyExtId) >= MaxImagesPerAvalanche) {
          ResponseWithReason(BadResponse(), getMessage("avyReportMaxImagesExceeded", MaxImagesPerAvalanche).toString)
        } else {
          val fph = req.uploadedFiles(0)
          dao insertAvalancheImage AvalancheImage(avyExtId,
            fph.fileName.split("\\.")(0), fph.mimeType, fph.length.toInt, fph.file)
          JsonResponse(("extId" -> avyExtId) ~ ("fileName" -> fph.fileName) ~ ("fileSize" -> fph.length))
        }
      }
      response
    }
    
    case "rest" :: "images" :: avyExtId :: filename :: Nil Delete req => {
      try {
        transaction { 
          dao.deleteAvalancheImage(avyExtId, filename) 
        }
        OkResponse()
      } catch {
        case ue: UnauthorizedException => UnauthorizedResponse("Avy Eyes auth required")
        case e: Exception => InternalServerErrorResponse()
      }
    }
  }
}
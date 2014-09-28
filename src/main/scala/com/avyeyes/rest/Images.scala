package com.avyeyes.rest

import java.io.ByteArrayInputStream
import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.model._
import com.avyeyes.persist._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.StreamingResponse
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.NotFoundResponse

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
        case _ => new NotFoundResponse(s"Image $avyExtId/$filename not found")
      }
    }
    
    case "rest" :: "images" :: avyExtId :: Nil Post req => {
      val fph = req.uploadedFiles(0)
      transaction {
        dao insertAvalancheImage AvalancheImage(avyExtId, fph.fileName.split("\\.")(0), fph.mimeType, fph.file)
      }

      ("extId" -> avyExtId) ~ ("fileName" -> fph.fileName) ~ ("fileSize" -> fph.length)
    }
  }
}
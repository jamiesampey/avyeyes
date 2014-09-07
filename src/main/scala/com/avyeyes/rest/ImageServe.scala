package com.avyeyes.rest

import java.io.ByteArrayInputStream

import org.squeryl.PrimitiveTypeMode._

import com.avyeyes.persist._

import net.liftweb.http.BadResponse
import net.liftweb.http.StreamingResponse
import net.liftweb.http.rest.RestHelper

object ImageServe extends RestHelper {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  
  serve {
    case "rest" :: "imgserve" :: avyExtId :: filename :: Nil Get req => {
      val returnedImg = transaction { 
        dao.selectAvalancheImage(avyExtId, filename) 
      }
      
      returnedImg match {
        case Some(avyImg) => val byteStream = new ByteArrayInputStream(avyImg.bytes)
          StreamingResponse(byteStream, () => byteStream.close(), avyImg.bytes.length, 
            ("Content-Type", avyImg.mimeType) :: Nil, Nil, 200)
        case _ => new BadResponse
      }
    }
  }
}
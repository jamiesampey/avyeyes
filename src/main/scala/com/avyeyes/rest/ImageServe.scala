package com.avyeyes.rest

import net.liftweb.http.rest.RestHelper
import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.model.AvalancheDb
import java.io.ByteArrayInputStream
import net.liftweb.http.StreamingResponse
import net.liftweb.http.BadResponse

object ImageServe extends RestHelper {
    serve {
          case "rest" :: "imgserve" :: avyExtId :: filename :: Nil Get req => {
              val returnedImg = transaction {
                 from(AvalancheDb.avalancheImageDropbox)(img => 
                   where(img.avyExtId === avyExtId and img.filename === filename) 
                   select(img)).headOption
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
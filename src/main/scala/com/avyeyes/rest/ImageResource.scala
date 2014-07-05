package com.avyeyes.rest

import java.io.ByteArrayInputStream

import org.squeryl.PrimitiveTypeMode._

import com.avyeyes.model.AvalancheDb
import com.avyeyes.model.AvalancheImg

import net.liftweb.http.BadResponse
import net.liftweb.http.FileParamHolder
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.JsonResponse
import net.liftweb.http.StreamingResponse
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonDSL._

object ImageResource extends RestHelper {
  
  serve {
    case "imgupload" :: avyExtId :: Nil Post req => {
         transaction {
             req.uploadedFiles foreach (fph => AvalancheDb.avalancheImages insert
               new AvalancheImg(avyExtId, parseFilename(fph), fph.mimeType, fph.file))
         }

        val ret = ("extId" -> avyExtId) ~ 
            ("filenames" -> List[JArray](req.uploadedFiles map (fph => parseFilename(fph))))

        val jr = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
        InMemoryResponse(jr.data, ("Content-Length", jr.data.length.toString) ::
            ("Content-Type", "text/plain") :: Nil, Nil, 200)
    }
    
    case "imgserve" :: avyExtId :: filename :: Nil Get req => {
      val returnedImg = transaction {
         from(AvalancheDb.avalancheImages)(img => 
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
  
  private def parseFilename(fph: FileParamHolder) = fph.fileName.split("\\.")(0)
}
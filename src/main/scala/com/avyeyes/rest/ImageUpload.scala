package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.model.AvalancheDb
import com.avyeyes.model.AvalancheImg
import com.avyeyes.util.AEConstants.JSON_MIME_TYPE
import net.liftweb.http.FileParamHolder
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.JsonResponse
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonDSL._

object ImageUpload extends RestHelper {
  
  serve {
    case "rest" :: "imgupload" :: avyExtId :: Nil Post req => {
         val fph = req.uploadedFiles(0)
         transaction {
             AvalancheDb.avalancheImageDropbox insert
               new AvalancheImg(avyExtId, fph.fileName.split("\\.")(0), fph.mimeType, fph.file)
         }

        val ret = ("extId" -> avyExtId) ~ ("fileName" -> fph.fileName) ~ ("fileSize" -> fph.length)

        val jr = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
        InMemoryResponse(jr.data, ("Content-Length", jr.data.length.toString) ::
            ("Content-Type", JSON_MIME_TYPE) :: Nil, Nil, 200)
    }
  }
}
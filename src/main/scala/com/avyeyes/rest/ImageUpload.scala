package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.model.AvalancheDb
import com.avyeyes.model.AvalancheImg
import net.liftweb.http.FileParamHolder
import net.liftweb.json.JsonDSL._
import net.liftweb.http.rest.RestHelper

object ImageUpload extends RestHelper with JsonResponder {
  
  serve {
    case "rest" :: "imgupload" :: avyExtId :: Nil Post req => {
         val fph = req.uploadedFiles(0)
         transaction {
             AvalancheDb.avalancheImageDropbox insert
               new AvalancheImg(avyExtId, fph.fileName.split("\\.")(0), fph.mimeType, fph.file)
         }

        val ret = ("extId" -> avyExtId) ~ ("fileName" -> fph.fileName) ~ ("fileSize" -> fph.length)

        sendJsonResponse(ret)
    }
  }
}
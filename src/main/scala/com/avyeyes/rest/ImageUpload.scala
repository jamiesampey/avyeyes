package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction

import com.avyeyes.persist.SquerylPersistence
import com.avyeyes.service.AvalancheService

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonDSL._

object ImageUpload extends RestHelper with JsonResponder with AvalancheService with SquerylPersistence {
  serve {
    case "rest" :: "imgupload" :: avyExtId :: Nil Post req => {
      val fph = req.uploadedFiles(0)
      transaction {
        insertAvalancheImage(avyExtId, fph)
      }

      val ret = ("extId" -> avyExtId) ~ ("fileName" -> fph.fileName) ~ ("fileSize" -> fph.length)
      sendJsonResponse(ret)
    }
  }
}
package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction
import com.avyeyes.model._
import com.avyeyes.persist._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonDSL._

object ImageUpload extends RestHelper {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
    
  serve {
    case "rest" :: "imgupload" :: avyExtId :: Nil Post req => {
      val fph = req.uploadedFiles(0)
      transaction {
        dao insertAvalancheImage AvalancheImg(avyExtId, fph.fileName.split("\\.")(0), fph.mimeType, fph.file)
      }

      ("extId" -> avyExtId) ~ ("fileName" -> fph.fileName) ~ ("fileSize" -> fph.length)
    }
  }
}
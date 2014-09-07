package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.AEHelpers.getRemoteIP
import net.liftweb.http.S
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import com.avyeyes.persist._

object ExtIdVendor extends RestHelper with JsonResponder with ExternalIdService {
  implicit lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  
  serve {
    case "rest" :: "reserveExtId" :: Nil Get req => {
      val newExtId = transaction {
        reserveNewExtId
      }
      logger.info(s"Served external id request from ${getRemoteIP(S.containerRequest)} with new extId $newExtId")
      sendJsonResponse(JObject(List(JField("extId", JString(newExtId)))))
    }
  }
}
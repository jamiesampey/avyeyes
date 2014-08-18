package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction
import net.liftweb.http.rest.RestHelper
import com.avyeyes.model.AvalancheDb
import com.avyeyes.util.AEHelpers._
import net.liftweb.json.JsonAST._
import net.liftweb.http.OkResponse
import net.liftweb.common.Loggable
import net.liftweb.http.S

object ExtIdVendor extends RestHelper with JsonResponder with Loggable {
    serve {
      case "rest" :: "reserveExtId" :: Nil Get req => {
        val newExtId = transaction {
             AvalancheDb.reserveNewExtId
        }
        logger.info("Serving external id request from " + getRemoteIP(S.containerRequest) 
            + " with new extId " + newExtId)
        sendJsonResponse(JObject(List(JField("extId", JString(newExtId)))))
      }
      
      case "rest" :: "unreserveExtId" :: extId :: Nil Post req => {
        AvalancheDb.unreserveExtId(extId)
        logger.info("Serving external id unreserve request from " + getRemoteIP(S.containerRequest) 
            + " for external id " + extId)
        new OkResponse
      }
    }
}
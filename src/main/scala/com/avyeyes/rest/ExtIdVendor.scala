package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction
import net.liftweb.http.rest.RestHelper
import com.avyeyes.model.AvalancheDb
import net.liftweb.json.JsonAST._
import net.liftweb.http.OkResponse

object ExtIdVendor extends RestHelper with JsonResponder {
    serve {
      case "rest" :: "reserveExtId" :: Nil Get req => {
        val newExtId = transaction {
             AvalancheDb.reserveNewExtId
        }
        sendJsonResponse(JObject(List(JField("extId", JString(newExtId)))))
      }
      
      case "rest" :: "unreserveExtId" :: extId :: Nil Post req => {
        AvalancheDb.unreserveExtId(extId)
        new OkResponse
      }
    }
}
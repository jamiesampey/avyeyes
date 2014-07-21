package com.avyeyes.rest

import net.liftweb.http.rest.RestHelper
import com.avyeyes.model.AvalancheDb
import net.liftweb.json.JsonAST._
import net.liftweb.http.OkResponse

object ExtIdVendor extends RestHelper with JsonResponder {
    serve {
      case "rest" :: "reserveExtId" :: Nil Get req => {
        val ret = JObject(List(JField("extId", JString(AvalancheDb.reserveNewExtId))))
        sendJsonResponse(ret)
      }
      
      case "rest" :: "unreserveExtId" :: extId :: Nil Post req => {
        AvalancheDb.unreserveExtId(extId)
        new OkResponse
      }
    }
}
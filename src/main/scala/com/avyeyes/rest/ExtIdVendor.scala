package com.avyeyes.rest

import net.liftweb.http.rest.RestHelper
import com.avyeyes.model.AvalancheDb
import com.avyeyes.util.AEConstants.JSON_MIME_TYPE
import net.liftweb.http.JsonResponse
import net.liftweb.json.JsonAST._
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.OkResponse

object ExtIdVendor extends RestHelper {
    serve {
      case "rest" :: "reserveExtId" :: Nil Get req => {
        val ret = JObject(List(JField("extId", JString(AvalancheDb.reserveNewExtId))))
        val jr = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]

        InMemoryResponse(jr.data, ("Content-Length", jr.data.length.toString) ::
            ("Content-Type", JSON_MIME_TYPE) :: Nil, Nil, 200)
      }
      
      case "rest" :: "unreserveExtId" :: extId :: Nil Post req => {
        AvalancheDb.unreserveExtId(extId)
        new OkResponse
      }
    }
}
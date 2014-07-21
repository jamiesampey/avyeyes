package com.avyeyes.rest

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.JsonResponse
import net.liftweb.json.JsonAST.JObject
import net.liftweb.http.InMemoryResponse

trait JsonResponder {
    val JSON_MIME_TYPE = "application/json"
    
    def sendJsonResponse(jobj: JObject) = {
        val jr = JsonResponse(jobj).toResponse.asInstanceOf[InMemoryResponse]
        InMemoryResponse(jr.data, ("Content-Length", jr.data.length.toString) ::
            ("Content-Type", JSON_MIME_TYPE) :: Nil, Nil, 200)
    }
}
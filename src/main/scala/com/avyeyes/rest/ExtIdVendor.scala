package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction

import com.avyeyes.model.AvalancheDb
import com.avyeyes.util.AEHelpers.getRemoteIP

import net.liftweb.common.Loggable
import net.liftweb.http.S
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JString

object ExtIdVendor extends RestHelper with JsonResponder with Loggable {
    serve {
      case "rest" :: "reserveExtId" :: Nil Get req => {
        val newExtId = transaction {
             AvalancheDb.reserveNewExtId
        }
        logger.info("Served external id request from " + getRemoteIP(S.containerRequest) 
          + " with new extId " + newExtId)
        sendJsonResponse(JObject(List(JField("extId", JString(newExtId)))))
      }
    }
}
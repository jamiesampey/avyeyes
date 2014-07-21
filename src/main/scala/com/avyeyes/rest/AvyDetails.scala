package com.avyeyes.rest

import com.avyeyes.model.AvalancheDb
import com.avyeyes.model.Avalanche
import com.avyeyes.util.AEHelpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.BadResponse
import net.liftweb.http.rest.RestHelper
import com.avyeyes.model.enums.AvalancheType


object AvyDetails extends RestHelper with JsonResponder {
    serve {
      case "rest" :: "avydetails" :: extId :: Nil Get req => {
        val avalancheOption = AvalancheDb.getAvalancheByExtId(Some(extId))
        
        if (avalancheOption.isDefined) {
            sendJsonResponse(getJSON(avalancheOption.get))
        } else {
            new BadResponse
        }
      }
    }
    
    private def getJSON(a: Avalanche) = {
      ("areaName" -> a.areaName) ~ ("avyDate" -> a.avyDate.toString) ~
      ("avyType" -> AvalancheType.getEnumLabel(a.avyType))
    }
}
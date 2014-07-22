package com.avyeyes.rest

import com.avyeyes.model.AvalancheDb
import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
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
      ("extId" -> a.extId) ~ ("areaName" -> a.areaName) ~ ("avyDate" -> a.avyDate.toString) ~
      ("submitterExp" -> ExperienceLevel.getEnumLabel(a.submitterExp)) ~ 
      ("submitterYearsExp" -> a.submitterYearsExp) ~
      ("sky" -> Sky.getEnumLabel(a.sky)) ~ ("precip" -> Precip.getEnumLabel(a.precip)) ~
      ("elevation" -> a.elevation) ~ ("aspect" -> Aspect.getEnumLabel(a.aspect)) ~ ("angle" -> a.angle) ~
      ("avyType" -> AvalancheType.getEnumLabel(a.avyType)) ~
      ("trigger" -> AvalancheTrigger.getEnumLabel(a.trigger)) ~
      ("bedSurface" -> AvalancheInterface.getEnumLabel(a.bedSurface)) ~
      ("rSize" -> a.rSize) ~ ("dSize" -> a.dSize) ~
      ("caught" -> a.caught) ~ ("partiallyBuried" -> a.partiallyBuried) ~ ("fullyBuried" -> a.fullyBuried) ~ 
      ("injured" -> a.injured) ~ ("killed" -> a.killed) ~  
      ("modeOfTravel" -> ModeOfTravel.getEnumLabel(a.modeOfTravel)) ~
      ("comments" -> a.comments)
    }
}
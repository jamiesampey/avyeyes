package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction
import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.persist.SquerylPersistence
import com.avyeyes.util.AEHelpers.humanNumberToStr
import net.liftweb.http.BadResponse
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import com.avyeyes.service.PersistenceService


object AvyDetails extends RestHelper with JsonResponder with PersistenceService with SquerylPersistence {
  serve {
    case "rest" :: "avydetails" :: extId :: Nil Get req => {
      val avyJsonOption = transaction {
          val avalancheOption = findViewableAvalanche(extId)
          if (avalancheOption.isDefined) {
              Some(getJSON(avalancheOption.get))
          } else None
      }
      
      if (avyJsonOption.isDefined) {
          logger.debug("Serving details for avy " + extId)
          sendJsonResponse(avyJsonOption.get)
      } else {
          logger.warn("Avy details request failed. Could not serve details for avy " + extId)
          new BadResponse
      }
    }
  }

  private def getJSON(a: Avalanche) = {
    val imgFilenames = findAvalancheImageFilenames(a.extId).toList
    
    ("extId" -> a.extId) ~ ("areaName" -> a.areaName) ~ ("avyDate" -> a.avyDate.toString) ~
    ("submitterExp" -> ExperienceLevel.getEnumLabel(a.submitterExp)) ~ 
    ("sky" -> Sky.getEnumLabel(a.sky)) ~ ("precip" -> Precip.getEnumLabel(a.precip)) ~
    ("elevation" -> a.elevation) ~ ("aspect" -> Aspect.getEnumLabel(a.aspect)) ~ ("angle" -> a.angle) ~
    ("avyType" -> AvalancheType.getEnumLabel(a.avyType)) ~
    ("trigger" -> AvalancheTrigger.getEnumLabel(a.trigger)) ~
    ("bedSurface" -> AvalancheInterface.getEnumLabel(a.bedSurface)) ~
    ("rSize" -> a.rSize) ~ ("dSize" -> a.dSize) ~
    ("caught" -> humanNumberToStr(a.caught)) ~ ("partiallyBuried" -> humanNumberToStr(a.partiallyBuried)) ~ 
    ("fullyBuried" -> humanNumberToStr(a.fullyBuried)) ~ ("injured" -> humanNumberToStr(a.injured)) ~ 
    ("killed" -> humanNumberToStr(a.killed)) ~ ("modeOfTravel" -> ModeOfTravel.getEnumLabel(a.modeOfTravel)) ~
    ("comments" -> a.comments) ~ ("images" -> JArray(imgFilenames map (s => JString(s))))
  }
}
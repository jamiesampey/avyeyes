package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction
import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.persist._
import com.avyeyes.util.AEHelpers._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.common.Loggable
import net.liftweb.http.NotFoundResponse


object AvyDetails extends RestHelper with Loggable {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  
  serve {
    case "rest" :: "avydetails" :: extId :: Nil Get req => {
      val avyJsonOption = transaction {
          val avalancheOption = dao.selectViewableAvalanche(extId)
          if (avalancheOption.isDefined) {
              Some(getJSON(avalancheOption.get))
          } else None
      }
      
      if (avyJsonOption.isDefined) {
          logger.debug("Serving details for avy " + extId)
          avyJsonOption.get
      } else {
          logger.warn("Avy details request failed. Could not serve details for avy " + extId)
          NotFoundResponse("Avalanche not found")
      }
    }
  }

  private def getJSON(a: Avalanche) = {
    val imgFilenames = dao.selectAvalancheImageFilenames(a.extId).toList
    val extUrl = getHttpBaseUrl + a.extId
    
    ("extId" -> a.extId) ~ ("extUrl" -> extUrl) ~ ("areaName" -> a.areaName) ~ ("avyDate" -> a.avyDate.toString) ~
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
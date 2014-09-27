package com.avyeyes.rest

import org.squeryl.PrimitiveTypeMode.transaction
import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.persist._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.snippet.AdminConsole._

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
          val avalancheOption = dao.selectAvalanche(extId)
          
          if (avalancheOption.isDefined) {
            val avalanche = avalancheOption.get
            isAuthorizedSession match {
              case true => Some(getJson(avalanche) ~ getJsonAdminFields(avalanche))
              case false => Some(getJson(avalanche))
            }
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

  private def getJson(a: Avalanche) = {
    val imgFilenames = dao.selectAvalancheImageFilenames(a.extId).toList
    val extUrl = getHttpBaseUrl + a.extId
    
    ("extId" -> a.extId) ~ ("extUrl" -> extUrl) ~ 
    ("areaName" -> a.areaName) ~ ("avyDate" -> dateToStr(a.avyDate)) ~
    ("submitterExp" -> ExperienceLevel.toJObject(a.submitterExp)) ~ 
    ("sky" -> Sky.toJObject(a.sky)) ~ ("precip" -> Precip.toJObject(a.precip)) ~
    ("elevation" -> a.elevation) ~ ("aspect" -> Aspect.toJObject(a.aspect)) ~ ("angle" -> a.angle) ~
    ("avyType" -> AvalancheType.toJObject(a.avyType)) ~
    ("trigger" -> AvalancheTrigger.toJObject(a.trigger)) ~
    ("bedSurface" -> AvalancheInterface.toJObject(a.bedSurface)) ~
    ("rSize" -> a.rSize) ~ ("dSize" -> a.dSize) ~
    ("caught" -> a.caught) ~ ("partiallyBuried" -> a.partiallyBuried) ~ 
    ("fullyBuried" -> a.fullyBuried) ~ ("injured" -> a.injured) ~ 
    ("killed" -> a.killed) ~ ("modeOfTravel" -> ModeOfTravel.toJObject(a.modeOfTravel)) ~
    ("comments" -> a.comments) ~ ("images" -> JArray(imgFilenames map (s => JString(s))))
  }
  
  private def getJsonAdminFields(a: Avalanche) = {
    ("viewable", a.viewable) ~ ("submitterEmail", a.submitterEmail)
  }
}
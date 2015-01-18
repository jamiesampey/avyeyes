package com.avyeyes.rest

import com.avyeyes.persist.AvyEyesSqueryl.transaction
import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist._
import com.avyeyes.util.Helpers._
import com.avyeyes.snippet.AdminConsole._

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.common.Loggable
import net.liftweb.http.NotFoundResponse


object AvyDetails extends RestHelper with Loggable {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  
  serve {
    case "rest" :: "avydetails" :: extId :: Nil JsonGet req => {
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
    val imagesMetadata = dao.selectAvalancheImagesMetadata(a.extId)

    ("extId" -> a.extId) ~ ("extUrl" -> a.getExtHttpUrl) ~
    ("areaName" -> a.areaName) ~ ("avyDate" -> dateToStr(a.avyDate)) ~
    ("submitterExp" -> ExperienceLevel.toJObject(a.submitterExp)) ~ 
    ("sky" -> Sky.toJObject(a.sky)) ~ ("precip" -> Precip.toJObject(a.precip)) ~
    ("elevation" -> a.elevation) ~ ("aspect" -> Aspect.toJObject(a.aspect)) ~ ("angle" -> a.angle) ~
    ("avyType" -> AvalancheType.toJObject(a.avyType)) ~
    ("avyTrigger" -> AvalancheTrigger.toJObject(a.avyTrigger)) ~
    ("avyInterface" -> AvalancheInterface.toJObject(a.avyInterface)) ~
    ("rSize" -> a.rSize) ~ ("dSize" -> a.dSize) ~
    ("caught" -> a.caught) ~ ("partiallyBuried" -> a.partiallyBuried) ~ 
    ("fullyBuried" -> a.fullyBuried) ~ ("injured" -> a.injured) ~ 
    ("killed" -> a.killed) ~ ("modeOfTravel" -> ModeOfTravel.toJObject(a.modeOfTravel)) ~
    ("comments" -> a.comments) ~ 
    ("images" -> JArray(imagesMetadata map Function.tupled ((f,m,s) => imageMetadataToJObject(f,m,s))))
  }
  
  private def imageMetadataToJObject(filename: String, mimeType: String, size: Int): JObject = JObject(List(
    JField("filename", JString(filename)),
    JField("mimeType", JString(mimeType)),
    JField("size", JInt(size)) 
  ))
  
  private def getJsonAdminFields(a: Avalanche) = {
    ("viewable", a.viewable) ~ ("submitterEmail", a.submitter.single.email)
  }
}
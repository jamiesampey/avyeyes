package com.avyeyes.rest

import com.avyeyes.persist.AvyEyesSqueryl.transaction
import com.avyeyes.persist.DaoInjector
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.Helpers.getRemoteIP
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._


class ExtIdVendor extends RestHelper with ExternalIdService {
  implicit lazy val dao = DaoInjector.avalancheDao.vend
  
  serve {
    case "rest" :: "reserveExtId" :: Nil JsonGet req => {
      try {
        val newExtId = transaction {
          reserveNewExtId
        }
        logger.info(s"Served extId request from ${getRemoteIP(S.containerRequest)} with new extId $newExtId")
        JObject(List(JField("extId", JString(newExtId))))
      } catch {
        case rte: RuntimeException => {
          logger.error(s"Exception thrown while serving extId request from ${getRemoteIP(S.containerRequest)}", rte)
          InternalServerErrorResponse()
        }
      }
    }
  }
}
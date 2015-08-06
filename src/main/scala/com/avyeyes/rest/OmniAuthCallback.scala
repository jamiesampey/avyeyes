package com.avyeyes.rest

import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants.LoginPath
import net.liftweb.common._
import net.liftweb.http.RedirectResponse
import net.liftweb.http.rest.RestHelper
import omniauth.Omniauth

class OmniAuthCallback extends RestHelper with Loggable {
  lazy val userSession = Injectors.user.vend
  protected[rest] val RedirectUri = s"/$LoginPath"

  serve {
    case "auth" :: "omnisuccess" :: Nil Get req => {
      Omniauth.currentAuth match {
        case Full(authInfo) =>  authInfo.email match {
          case Some(email) => {
            logger.info(s"Oauth2 authentication success from ${authInfo.provider} for $email")
            userSession.attemptLogin(email)
          }
          case None => logger.warn("Received Oauth2 response without an email address")
        }
        case _ => logger.warn("Received REST call to omnisuccess endpoint with no authInfo")
      }

      Omniauth.clearCurrentAuth
      RedirectResponse(RedirectUri)
    }
    
    case "auth" :: "omnifailure" :: Nil Get req => {
      Omniauth.clearCurrentAuth
      RedirectResponse(RedirectUri)
    }
  }
}
package com.avyeyes.rest

import com.avyeyes.util.Constants.LoginPath
import com.avyeyes.util.UserSession
import net.liftweb.common._
import net.liftweb.http.RedirectResponse
import net.liftweb.http.rest.RestHelper
import omniauth.Omniauth

object OmniAuthCallback extends RestHelper with Loggable {
  val userSession = new UserSession

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
      RedirectResponse("/" + LoginPath)
    }
    
    case "auth" :: "omnifailure" :: Nil Get req => {
      Omniauth.clearCurrentAuth
      RedirectResponse("/" + LoginPath)
    }
  }
}
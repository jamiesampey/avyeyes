package com.avyeyes.rest

import com.avyeyes.persist.AvyEyesSqueryl.transaction

import com.avyeyes.persist._
import com.avyeyes.util.Constants.LoginPath

import net.liftweb.common._
import net.liftweb.http.RedirectResponse
import net.liftweb.http.rest.RestHelper
import omniauth.Omniauth

object OmniAuthCallback extends RestHelper with Loggable {
  lazy val userDao: UserDao = PersistenceInjector.userDao.vend
  
  serve {
    case "auth" :: "omnisuccess" :: Nil Get req => {
      var provider = ""
      val omniauthEmail = Omniauth.currentAuth match {
        case Full(authInfo) => provider = authInfo.provider; authInfo.email
        case _ => None
      }
      
      omniauthEmail match {
        case Some(email) => {
          logger.info(s"Oauth2 authentication success from $provider: $email")
          
          transaction {
            userDao.isUserAuthorized(email) match {
              case false => Omniauth.clearCurrentAuth
              case _ => // authorization success, leave Omniauth.currentAuth intact
            }
          }
        }
        case _ => Omniauth.clearCurrentAuth
      }
      RedirectResponse("/" + LoginPath)
    }
    
    case "auth" :: "omnifailure" :: Nil Get req => {
      Omniauth.clearCurrentAuth
      RedirectResponse("/" + LoginPath)
    }
  }
}
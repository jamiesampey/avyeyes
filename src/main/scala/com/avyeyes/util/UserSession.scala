package com.avyeyes.util

import com.avyeyes.data.DaoInjector
import net.liftweb.common._
import net.liftweb.http.SessionVar
import omniauth.Omniauth

private object authorizedEmail extends SessionVar[Box[String]](Empty)

class UserSession extends Loggable {
  lazy val dao = DaoInjector.dao.vend

  def isAuthorizedSession() = authorizedEmail.get.isDefined

  def getAuthorizedEmail() = {
    authorizedEmail.get match {
      case Full(email) => email
      case _ => ""
    }
  }

  def attemptLogin(email: String) = {
    dao.isUserAuthorized(email) match {
      case true => {
        logger.info (s"Authorization success for $email. Logging user in.")
        authorizedEmail.set(Full(email))
      }
      case false => {
        logger.warn(s"Authorization failure for $email.")
        authorizedEmail.set(Empty)
      }

      Omniauth.clearCurrentAuth
    }
  }

  def logout = {
    logger.info(s"logging out authorized user ${getAuthorizedEmail}")
    authorizedEmail.set(Empty)
    Omniauth.clearCurrentAuth
  }
}

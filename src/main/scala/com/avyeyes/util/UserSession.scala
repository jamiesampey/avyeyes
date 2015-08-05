package com.avyeyes.util

import com.avyeyes.data.DalInjector
import net.liftweb.common._
import net.liftweb.http.SessionVar
import omniauth.Omniauth

import scala.concurrent.Await
import scala.concurrent.duration._

private object authorizedEmail extends SessionVar[Box[String]](Empty)

class UserSession extends Loggable {
  lazy val dal = DalInjector.dal.vend

  def isAuthorizedSession() = authorizedEmail.get.isDefined

  def getAuthorizedEmail() = {
    authorizedEmail.get match {
      case Full(email) => email
      case _ => ""
    }
  }

  def attemptLogin(email: String) = {
    Await.result(dal.isUserAuthorized(email), 30 seconds) match {
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

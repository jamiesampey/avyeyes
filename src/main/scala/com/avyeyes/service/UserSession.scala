package com.avyeyes.service

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Constants._
import com.avyeyes.util.FutureOps._
import net.liftweb.common._
import net.liftweb.http.SessionVar
import omniauth.Omniauth
import org.joda.time.{DateTime, Seconds}

private object adminEmail extends SessionVar[Box[String]](Empty)

class UserSession extends ExternalIdService with Loggable {
  lazy val dal = Injectors.dal.vend

  def isAdminSession = adminEmail.get.isDefined

  def isAuthorizedToViewAvalanche(avalanche: Avalanche): Boolean = isAdminSession || avalanche.viewable

  def isAuthorizedToEditAvalanche(avyExtId: String, editKeyBox: Box[String]): Boolean = {
    logger.info(s"TEST 1 isAdminSession is ${isAdminSession}")
    reservationExists(avyExtId) || dal.getAvalanche(avyExtId).exists(isAuthorizedToEditAvalanche(_, editKeyBox))
  }

  def isAuthorizedToEditAvalanche(avalanche: Avalanche, editKeyBox: Box[String]): Boolean = {
    logger.info(s"TEST 2 isAdminSession is ${isAdminSession}")
    isAdminSession || reservationExists(avalanche.extId) || (editKeyBox match {
      case Full(editKey) if editKey.toLong == avalanche.editKey =>
        Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
      case _ => false
    })
  }

  def authorizedEmail = adminEmail.get match {
    case Full(email) => email
    case _ => ""
  }

  def attemptLogin(email: String) = {
    val userRoles = dal.userRoles(email).resolve
    logger.info(s"userRoles: $userRoles")

    userRoles.exists(user => user.role == SiteOwnerRole || user.role == AdminRole) match {
      case true =>
        logger.info (s"Authorization success for $email. Logging user in.")
        adminEmail.set(Full(email))
      case false =>
        logger.warn(s"Authorization failure for $email.")
        adminEmail.set(Empty)
        Omniauth.clearCurrentAuth
    }
  }

  def logout = {
    logger.info(s"logging out authorized user $authorizedEmail")
    adminEmail.set(Empty)
    Omniauth.clearCurrentAuth
  }
}

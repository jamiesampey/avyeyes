package com.avyeyes.service

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Constants._
import com.avyeyes.util.FutureOps._
import net.liftweb.common._
import net.liftweb.http.SessionVar
import omniauth.Omniauth
import org.joda.time.{DateTime, Seconds}

private object adminEmailSessionVar extends SessionVar[Box[String]](Empty)

class UserSession extends ExternalIdService with Loggable {
  lazy val dal = Injectors.dal.vend

  def isAdminSession = adminEmailSessionVar.get.isDefined

  def isAuthorizedToViewAvalanche(avalanche: Avalanche): Boolean = isAdminSession || avalanche.viewable

  def isAuthorizedToEditAvalanche(avyExtId: String, editKeyBox: Box[String]): Boolean =
    reservationExists(avyExtId) || dal.getAvalanche(avyExtId).exists(isAuthorizedToEditAvalanche(_, editKeyBox))

  def isAuthorizedToEditAvalanche(avalanche: Avalanche, editKeyBox: Box[String]): Boolean = {
    isAdminSession || reservationExists(avalanche.extId) || (editKeyBox match {
      case Full(editKey) if editKey.toLong == avalanche.editKey =>
        Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
      case _ => false
    })
  }

  def authorizedEmail = adminEmailSessionVar.get match {
    case Full(email) => email
    case _ => ""
  }

  def attemptLogin(email: String) = {
    val userRoles = dal.userRoles(email).resolve
    userRoles.exists(user => user.role == SiteOwnerRole || user.role == AdminRole) match {
      case true =>
        logger.info(s"Authorization success for $email. Logging user in")
        setSessionVar(Full(email))
      case false =>
        logger.warn(s"Authorization failure for $email.")
        setSessionVar(Empty)
        Omniauth.clearCurrentAuth
      }
  }

  def logout = {
    logger.info(s"logging out authorized user $authorizedEmail")
    setSessionVar(Empty)
    Omniauth.clearCurrentAuth
  }

  private[service] def setSessionVar(emailBox: Box[String]) = adminEmailSessionVar.set(emailBox)
}

package com.avyeyes.snippet

import scala.xml.NodeSeq

import org.apache.commons.lang3.StringUtils._
import org.mindrot.jbcrypt.BCrypt

import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import com.avyeyes.util.AEHelpers._

object AdminLogin extends Loggable {
  private object loggedInEmail extends SessionVar[String]("")
  
  private val AccessDenied = getMessage("loginAccessDenied").toString
  private val AdminLoginEmailHash = Props.get("login.email", "")
  private val AdminLoginPwHash = Props.get("login.pw", "")
  
  def isAdminLoggedIn = isNotBlank(loggedInEmail.is)

  def loggedInContent(html: NodeSeq) = if (isAdminLoggedIn) html else NodeSeq.Empty
  def loggedOutContent(html: NodeSeq) = if (!isAdminLoggedIn) html else NodeSeq.Empty

  def getLoggedInEmail = "*" #> loggedInEmail.is
    
  def logIn = {
    var emailLoginAttempt = ""
    var passwordAttempt = ""
    
    def processLogIn() {
      if (isBlank(AdminLoginEmailHash) || isBlank(AdminLoginPwHash)) {
        logger.error("Could not retrieve admin email and/or password hashes")
        loggedInEmail.set("")
      } else {
        if (BCrypt.checkpw(emailLoginAttempt, AdminLoginEmailHash) 
            && BCrypt.checkpw(passwordAttempt, AdminLoginPwHash)) {
          loggedInEmail.set(emailLoginAttempt)
          logger.info(s"login success: $emailLoginAttempt")
        } else {
          logger.warn(s"login failure: $emailLoginAttempt")
          emailLoginAttempt = ""
          passwordAttempt = ""
          S.error("loginMsg", AccessDenied)
        }
      }
    }
    
    "#avyLoginEmail" #> SHtml.text(emailLoginAttempt, emailLoginAttempt = _) &
    "#avyLoginPassword" #> SHtml.password(passwordAttempt, passwordAttempt = _) &
    "#avyLoginButton" #> SHtml.onSubmitUnit(processLogIn)
  }

  def logOut = {
    def processLogOut() = loggedInEmail.set("")
    
    "#avyLogoutButton" #> SHtml.onSubmitUnit(processLogOut)
  }
}
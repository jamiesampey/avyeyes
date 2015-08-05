package com.avyeyes.snippet

import com.avyeyes.data.DalInjector
import com.avyeyes.service.UserInjector
import com.avyeyes.util.Helpers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import org.apache.commons.lang3.StringUtils._

import scala.xml.NodeSeq

class Admin extends Loggable {
  lazy val dal = DalInjector.dal.vend
  lazy val userSession = UserInjector.userSession.vend

  private val AccessDenied = getMessage("avyAdminLocalLoginAccessDenied").toString
  private val LocalAuthEmailHash = Props.get("localauth.email", "")
  private val LocalAuthPwHash = Props.get("localauth.pw", "")
  
  private def processLogout() = {
    userSession.logout
    S.redirectTo(getHttpsBaseUrl)
  }

  def localLogIn = {
    var email = ""
    var pw = ""
    
    def processLocalLogin() {
      if (isBlank(LocalAuthEmailHash) || isBlank(LocalAuthPwHash)) {
        logger.error("Could not retrieve local auth email and/or password hashes from props")
        userSession.logout
      } else {
        if (BCrypt.checkpw(email, LocalAuthEmailHash)
            && BCrypt.checkpw(pw, LocalAuthPwHash)) {
          logger.info(s"local authentication success for $email")
          userSession.attemptLogin(email)
        } else {
          logger.warn(s"local authentication failure for $email")
        }
        
        if (!userSession.isAuthorizedSession) {
          S.error("avyAdminLocalLoginMsg", AccessDenied)
        }

        email = ""
        pw = ""
      }
    }
    
    "#avyAdminLocalLoginEmail" #> SHtml.text(email, email = _) &
    "#avyAdminLocalLoginPw" #> SHtml.password(pw, pw = _) &
    "#avyAdminLocalLoginButton" #> SHtml.onSubmitUnit(processLocalLogin)
  }

  def logOut = "#avyAdminLogoutButton" #> SHtml.onSubmitUnit(processLogout)
  
  def loggedOutContent(html: NodeSeq) = if (!userSession.isAuthorizedSession) html else NodeSeq.Empty
  def loggedInContent(html: NodeSeq) = if (userSession.isAuthorizedSession) html else NodeSeq.Empty
  
  def unviewableAvalancheCount() = <span>{dal.countAvalanches(Some(false)) }</span>
  def viewableAvalancheCount() = <span>{dal.countAvalanches(Some(true)) }</span>
}
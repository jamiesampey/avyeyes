package com.avyeyes.snippet

import scala.xml.NodeSeq
import org.apache.commons.lang3.StringUtils._
import org.mindrot.jbcrypt.BCrypt
import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.persist._
import com.avyeyes.util.AEHelpers._
import net.liftweb.common._
import net.liftweb.common.Box._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import omniauth._
import com.avyeyes.model.User

object AdminConsole extends Loggable {
  lazy val userDao: UserDao = PersistenceInjector.userDao.vend

  def isAuthorizedSession(): Boolean = isNotBlank(authorizedEmail)
  def authorizedEmail(): String = {
    localAuthorizedEmail.get match {
      case Full(localEmail) => localEmail
      case _ => Omniauth.currentAuth match {
        case Full(omniAuthInfo) => omniAuthInfo.email openOr ""
        case _ => ""
      }
    }
  }
  
  private object localAuthorizedEmail extends SessionVar[Box[String]](Empty)
    
  private def clearAuthorizedEmails() = {
    logger.info(s"logging out authorized user ${authorizedEmail}")
    localAuthorizedEmail.set(Empty)
    Omniauth.clearCurrentAuth
  }
  
  private val AccessDenied = getMessage("loginAccessDenied").toString
  private val LocalAuthEmailHash = Props.get("localauth.email", "")
  private val LocalAuthPwHash = Props.get("localauth.pw", "")

  def logoutForm(html: NodeSeq) = if (isAuthorizedSession) html else NodeSeq.Empty
  def loginForm(html: NodeSeq) = if (!isAuthorizedSession) html else NodeSeq.Empty

  def loggedInEmailNodeSeq = "*" #> authorizedEmail
  
  def localLogIn = {
    var localLoginAttemptEmail = ""
    var localLoginAttemptPw = ""
    
    def processLocalLogin() {
      if (isBlank(LocalAuthEmailHash) || isBlank(LocalAuthPwHash)) {
        logger.error("Could not retrieve local auth email and/or password hashes from props")
        localAuthorizedEmail.set(Empty)
      } else {
        if (BCrypt.checkpw(localLoginAttemptEmail, LocalAuthEmailHash) 
            && BCrypt.checkpw(localLoginAttemptPw, LocalAuthPwHash)) {
          logger.info(s"local authentication success: $localLoginAttemptEmail")
          
          transaction {
            userDao.isUserAuthorized(localLoginAttemptEmail) match {
              case true => localAuthorizedEmail.set(Full(localLoginAttemptEmail))
              case false => localAuthorizedEmail.set(Empty)
            }
          }
                
        } else {
          logger.warn(s"local authentication failure: $localLoginAttemptEmail")
          localAuthorizedEmail.set(Empty)
          localLoginAttemptEmail = ""
          localLoginAttemptPw = ""
          S.error("loginMsg", AccessDenied)
        }
      }
    }
    
    "#avyLoginEmail" #> SHtml.text(localLoginAttemptEmail, localLoginAttemptEmail = _) &
    "#avyLoginPassword" #> SHtml.password(localLoginAttemptPw, localLoginAttemptPw = _) &
    "#avyLoginButton" #> SHtml.onSubmitUnit(processLocalLogin)
  }

  def logOut = {
    "#avyLogoutButton" #> SHtml.onSubmitUnit(clearAuthorizedEmails)
  }
}
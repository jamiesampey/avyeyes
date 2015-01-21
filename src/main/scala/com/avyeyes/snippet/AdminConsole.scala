package com.avyeyes.snippet

import scala.xml.NodeSeq
import org.apache.commons.lang3.StringUtils._
import com.avyeyes.persist._
import com.avyeyes.model._
import com.avyeyes.util.Helpers._
import net.liftweb.common._
import net.liftweb.common.Box._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import omniauth._
import com.avyeyes.persist.AvyEyesSqueryl._
import java.text.SimpleDateFormat

object AdminConsole extends Loggable {
  lazy val avyDao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  lazy val userDao: UserDao = PersistenceInjector.userDao.vend

  private object localAuthorizedEmail extends SessionVar[Box[String]](Empty)
    
  private val AccessDenied = getMessage("avyAdminLocalLoginAccessDenied").toString
  private val LocalAuthEmailHash = Props.get("localauth.email", "")
  private val LocalAuthPwHash = Props.get("localauth.pw", "")
  
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
  
  private def processLogout() = {
    logger.info(s"logging out authorized user ${authorizedEmail}")
    localAuthorizedEmail.set(Empty)
    Omniauth.clearCurrentAuth
    S.redirectTo(getHttpsBaseUrl)
  }

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
        }
        
        if (localAuthorizedEmail.is.isEmpty) {
          localLoginAttemptEmail = ""
          localLoginAttemptPw = ""
          S.error("avyAdminLocalLoginMsg", AccessDenied)
        }
      }
    }
    
    "#avyAdminLocalLoginEmail" #> SHtml.text(localLoginAttemptEmail, localLoginAttemptEmail = _) &
    "#avyAdminLocalLoginPw" #> SHtml.password(localLoginAttemptPw, localLoginAttemptPw = _) &
    "#avyAdminLocalLoginButton" #> SHtml.onSubmitUnit(processLocalLogin)
  }

  def logOut = "#avyAdminLogoutButton" #> SHtml.onSubmitUnit(processLogout)
  
  def loggedOutContent(html: NodeSeq) = if (!isAuthorizedSession) html else NodeSeq.Empty
  def loggedInContent(html: NodeSeq) = if (isAuthorizedSession) html else NodeSeq.Empty
  
  def unviewableAvalancheCount() = <span>{transaction {avyDao.countAvalanches(Some(false))} }</span>
  def viewableAvalancheCount() = <span>{transaction {avyDao.countAvalanches(Some(true))} }</span>
}
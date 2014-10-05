package com.avyeyes.snippet

import scala.xml.NodeSeq
import org.apache.commons.lang3.StringUtils._
import org.mindrot.jbcrypt.BCrypt
import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.persist._
import com.avyeyes.model._
import com.avyeyes.util.AEHelpers._
import net.liftweb.common._
import net.liftweb.common.Box._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import omniauth._
import org.squeryl.PrimitiveTypeMode._
import java.text.SimpleDateFormat

object AdminConsole extends Loggable {
  lazy val avyDao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  lazy val userDao: UserDao = PersistenceInjector.userDao.vend

  private object localAuthorizedEmail extends SessionVar[Box[String]](Empty)
    
  private val AccessDenied = getMessage("avyAdminLocalLoginAccessDenied").toString
  private val LocalAuthEmailHash = Props.get("localauth.email", "")
  private val LocalAuthPwHash = Props.get("localauth.pw", "")
  
  private val unviewableQuery = AvalancheQuery.baseQuery.copy(
    viewable = Some(false), orderBy = OrderBy.CreateTime, order = Order.Asc)
  private val recentlyUpdatedQuery = AvalancheQuery.baseQuery.copy(
    orderBy = OrderBy.UpdateTime, order = Order.Desc, offset = 0, limit = 50)
  
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    
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
    S.redirectTo(getHttpBaseUrl)
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
  
  def unviewableAvalancheCount() = <span>{transaction {avyDao.countAvalanches(false)} }</span>
  def viewableAvalancheCount() = <span>{transaction {avyDao.countAvalanches(true)} }</span>
  
  def unviewableAvalanches() = {
    val unviewableList = transaction { 
      avyDao.selectAvalanches(unviewableQuery) 
    }

    "tbody tr" #> unviewableList.map(a => <tr><td>{s"${sdf.format(a.createTime)}:"}</td><td>{getAvalancheLink(a)}</td></tr>)
  }
  
  def updatedAvalanches() = {
    val recentlyUpdatedList = transaction { 
      avyDao.selectAvalanches(recentlyUpdatedQuery)
    }
       
    "tbody tr" #> recentlyUpdatedList.map(a => <tr><td>{s"${sdf.format(a.updateTime)}:"}</td><td>{getAvalancheLink(a)}</td></tr>)
  }
  
  private def getAvalancheLink(a: Avalanche) = {
    <a href={getHttpBaseUrl + a.extId} target="_blank">{s"${a.areaName} (${a.extId})"}</a>
  }
}
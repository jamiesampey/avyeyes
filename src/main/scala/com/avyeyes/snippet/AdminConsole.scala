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

object AdminConsole extends Loggable {
  lazy val avyDao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  lazy val userDao: UserDao = PersistenceInjector.userDao.vend

  private val unviewableQuery = AvalancheQuery(Some(false), None, 
      "", "", "", "", "", "", "", "", "createTime", OrderDirection.ASC)
  private val recentlyUpdatedQuery = AvalancheQuery(None, None, 
      "", "", "", "", "", "", "", "", "updateTime", OrderDirection.DESC, 0, 50)
  
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
    
  private def processLogout() = {
    logger.info(s"logging out authorized user ${authorizedEmail}")
    localAuthorizedEmail.set(Empty)
    Omniauth.clearCurrentAuth
    S.redirectTo(getHttpBaseUrl)
  }

  private val AccessDenied = getMessage("avyAdminLocalLoginAccessDenied").toString
  private val LocalAuthEmailHash = Props.get("localauth.email", "")
  private val LocalAuthPwHash = Props.get("localauth.pw", "")
  
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
  
  def unapprovedAvalanches() = {
    val unapprovedList = transaction {
      avyDao.selectAvalanches(unviewableQuery)
    }
    renderAvalancheListAsTableRows(unapprovedList)
  }
  
  def updatedAvalanches() = {
    val recentlyUpdatedList = transaction {
      avyDao.selectAvalanches(recentlyUpdatedQuery)
    }
    renderAvalancheListAsTableRows(recentlyUpdatedList)
  }
  
  private def renderAvalancheListAsTableRows(list: List[Avalanche]): CssSel = {
    val baseUrl = getHttpBaseUrl

    "tbody tr" #> list.map(a => {
      <td>{s"${dateToStr(a.createTime)}:"}</td>
      <td><a href={baseUrl + a.extId} target="_blank">{s"${a.areaName} (${a.extId})"}</a></td>
    })
  }
}
//package com.avyeyes.controllers.snippet
//
//import com.avyeyes.data.CachedDAL
//import com.avyeyes.service.{Injectors, ConfigurationService, UserSession}
//import com.google.inject.Inject
//import net.liftweb.common._
//import net.liftweb.http._
//import net.liftweb.util.Helpers._
//import net.liftweb.util._
//import org.apache.commons.lang3.StringUtils._
//import play.Logger
//
//import scala.xml.NodeSeq
//
//class Admin @Inject()(dal: CachedDAL, user: UserSession, R: ConfigurationService) {
//  private val logger = Logger.of(Admin.class)
////  private val dal = Injectors.dal.vend
////  private val user = Injectors.user.vend
////  private val R = Injectors.resources.vend
//
//  private val AccessDenied = R.localizedString("msg.avyAdminLocalLoginAccessDenied")
//  private val LocalAuthEmailHash = R.getProperty("localauth.email")
//  private val LocalAuthPwHash = R.getProperty("localauth.pw")
//
//  private def processLogout() = {
//    user.logout
//    S.redirectTo(R.adminLoginUrl)
//  }
//
//  def localLogIn = {
//    var email = ""
//    var pw = ""
//
//    def processLocalLogin() {
//      if (isBlank(LocalAuthEmailHash) || isBlank(LocalAuthPwHash)) {
//        logger.error("Could not retrieve local auth email and/or password hashes from props")
//        user.logout
//      } else {
//        if (BCrypt.checkpw(email, LocalAuthEmailHash)
//            && BCrypt.checkpw(pw, LocalAuthPwHash)) {
//          logger.info(s"local authentication success for $email")
//          user.attemptLogin(email)
//        } else {
//          logger.warn(s"local authentication failure for $email")
//        }
//
//        if (!user.isAdminSession) {
//          S.error("avyAdminLocalLoginMsg", AccessDenied)
//        }
//
//        email = ""
//        pw = ""
//      }
//    }
//
//    "#avyAdminLocalLoginEmail" #> SHtml.text(email, email = _) &
//    "#avyAdminLocalLoginPw" #> SHtml.password(pw, pw = _) &
//    "#avyAdminLocalLoginButton" #> SHtml.onSubmitUnit(processLocalLogin)
//  }
//
//  def logOut = "#avyAdminLogoutButton" #> SHtml.onSubmitUnit(processLogout)
//
//  def loggedOutContent(html: NodeSeq) = if (!user.isAdminSession) html else NodeSeq.Empty
//  def loggedInContent(html: NodeSeq) = if (user.isAdminSession) html else NodeSeq.Empty
//
//  def unviewableAvalancheCount() = <span>{dal.countAvalanches(Some(false)) }</span>
//  def viewableAvalancheCount() = <span>{dal.countAvalanches(Some(true)) }</span>
//}
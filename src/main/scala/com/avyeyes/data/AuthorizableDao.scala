package com.avyeyes.data

import com.avyeyes.util.UserSession
import scala.concurrent.Future

trait AuthorizableDao {
  def isUserAuthorized(email: String): Future[Boolean]
  def isAuthorizedSession(implicit userSession: UserSession) = userSession.isAuthorizedSession()
}

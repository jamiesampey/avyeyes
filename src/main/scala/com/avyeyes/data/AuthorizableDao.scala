package com.avyeyes.data

import com.avyeyes.util.UserSession

trait AuthorizableDao {
  def isAuthorizedSession(implicit userSession: UserSession) = userSession.isAuthorizedSession()
}

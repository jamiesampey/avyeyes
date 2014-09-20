package com.avyeyes.persist

import com.avyeyes.model.User

trait UserDao {
  def selectUser(email: String): Option[User]
  def isUserAuthorized(email: String): Boolean
}
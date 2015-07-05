package com.avyeyes.model

trait UserDao {
  def selectUser(email: String): Option[User]
  def isUserAuthorized(email: String): Boolean
}
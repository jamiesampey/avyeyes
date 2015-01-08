package com.avyeyes.persist

import com.avyeyes.model.{User, UserRole}
import com.avyeyes.persist.AvyEyesSchema._
import net.liftweb.common.Loggable
import com.avyeyes.persist.AvyEyesSqueryl._

class SquerylUserDao extends UserDao with Loggable {
  def selectUser(email: String): Option[User] = {
    users.where(u => u.email === email).headOption
  }
  
  def isUserAuthorized(email: String): Boolean = {
    val usersRoles = selectUser(email) match {
      case Some(user) => user.roles.toList map (ur => ur.name)
      case None => Nil
    }

    if (usersRoles.contains(UserRole.SiteOwner) || usersRoles.contains(UserRole.Admin)) {
      logger.info(s"authorization success: $email")
      return true
    } else {
      logger.warn(s"authorization failure: $email")
      return false
    }
  }
}
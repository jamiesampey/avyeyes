package com.avyeyes.persist

import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.model.User
import com.avyeyes.persist.AvyEyesSchema._
import net.liftweb.common.Loggable

class SquerylUserDao extends UserDao with Loggable {
  def selectUser(email: String): Option[User] = {
    users.where(u => u.email === email).headOption
  }
  
  def isUserAuthorized(email: String): Boolean = {
    val authorizedUser = selectUser(email)
          
    authorizedUser match {
      case Some(user) => {
        logger.info(s"authorization success: $email")
        true
      }
      case _ => {
        logger.warn(s"authorization failure: $email")
        false
      }
    }
  }
}
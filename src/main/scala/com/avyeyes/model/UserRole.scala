package com.avyeyes.model

import com.avyeyes.persist.AvyEyesSchema

object UserRole {
  val SiteOwner = "site_owner"
  val Admin = "admin"
}

case class UserRole(name: String) extends SquerylDbObj {
  lazy val users = AvyEyesSchema.userRoleAssignments.right(this)
}

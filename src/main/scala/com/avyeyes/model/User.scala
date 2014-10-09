package com.avyeyes.model

import com.avyeyes.persist.AvyEyesSchema
import org.squeryl.dsl.OneToMany

case class User(email: String) extends SquerylDbObj {
  lazy val avalanches: OneToMany[Avalanche] = AvyEyesSchema.userToAvalanches.left(this)
  lazy val roles = AvyEyesSchema.userRoleAssignments.left(this)
}
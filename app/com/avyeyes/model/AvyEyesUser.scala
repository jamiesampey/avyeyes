package com.avyeyes.model

import org.joda.time.DateTime

case class AvyEyesUser(
  createTime: DateTime,
  lastActivityTime: DateTime,
  email: String,
  facebookId: Option[String],
  passwordHash: Option[String],
  roles: List[AvyEyesUserRole] = List.empty
)

case class AvyEyesUserRole(roleName: String)

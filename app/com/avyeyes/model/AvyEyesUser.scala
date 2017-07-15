package com.avyeyes.model

import org.joda.time.DateTime
import securesocial.core.BasicProfile

case class AvyEyesUser(
  createTime: DateTime,
  lastActivityTime: DateTime,
  email: String,
  profiles: List[BasicProfile] = List.empty,
  roles: List[AvyEyesUserRole] = List.empty
)

case class AvyEyesUserRole(roleName: String)

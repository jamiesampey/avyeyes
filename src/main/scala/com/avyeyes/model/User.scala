package com.avyeyes.model

import org.joda.time.DateTime

case class User(createTime: DateTime, email: String)

case class UserRole(email: String, role: String)

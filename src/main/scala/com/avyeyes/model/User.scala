package com.avyeyes.model

import org.joda.time.DateTime

trait User {
  def createTime: DateTime
  def email: String
}

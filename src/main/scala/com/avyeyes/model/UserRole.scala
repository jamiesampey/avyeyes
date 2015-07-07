package com.avyeyes.model

trait UserRole {
  val SiteOwner = "site_owner"
  val Admin = "admin"

  def name: String
}


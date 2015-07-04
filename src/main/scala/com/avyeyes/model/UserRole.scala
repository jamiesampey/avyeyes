package com.avyeyes.model

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime

object UserRole {
  val SiteOwner = "site_owner"
  val Admin = "admin"
}

case class UserRole(id: Long,
                    createTime: DateTime,
                    name: String)

class UserRoles(tag: Tag) extends Table[User](tag, "app_role") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def createTime = column[DateTime]("create_time")
  def name = column[String]("name")

  def * = ???
}

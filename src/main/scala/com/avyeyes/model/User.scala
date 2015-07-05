package com.avyeyes.model

import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

case class User(email: String) extends BaseDbObject

case class LiftedUser(email: Rep[String])

class Users(tag: Tag) extends Table[User](tag, "app_user") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def createTime = column[DateTime]("create_time")
  def email = column[String]("email")

  def * = (id, createTime, email).<> (User.apply _, User.unapply _)
}
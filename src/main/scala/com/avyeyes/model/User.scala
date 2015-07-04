package com.avyeyes.model

import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

case class User(id: Long,
                createTime: DateTime,
                email: String)

case class LiftedUser(id: Rep[Long],
                      createTime: Rep[DateTime],
                      email: Rep[String])

class Users(tag: Tag) extends Table[User](tag, "app_user") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def createTime = column[DateTime]("create_time")
  def email = column[String]("email")

  def * = ???
}
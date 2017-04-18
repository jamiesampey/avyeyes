package com.avyeyes.data

import javax.inject.Inject

import com.avyeyes.model.UserRole
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class UserDao @Inject()(@NamedDatabase("postgres") dbConfigProvider: DatabaseConfigProvider) extends DatabaseComponent with SlickColumnMappers {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  protected val jdbcProfile: JdbcProfile = dbConfig.profile
  import jdbcProfile.api._

  def userRoles(email: String): Future[Seq[UserRole]] = db.run(UserRoleRows.filter(_.email === email).result)
}

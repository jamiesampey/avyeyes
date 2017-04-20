package com.avyeyes.data

import javax.inject.Inject

import com.avyeyes.model.AvyEyesUser
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserDao @Inject()(@NamedDatabase("postgres") dbConfigProvider: DatabaseConfigProvider) extends DatabaseComponent with SlickColumnMappers {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  protected val jdbcProfile: JdbcProfile = dbConfig.profile
  import jdbcProfile.api._

  def findUser(email: String): Future[Option[AvyEyesUser]] = db.run(AppUserRows.filter(_.email === email).result.headOption).flatMap { appUserRowOpt =>
    db.run(AppUserRoleRows.filter(_.email === email).result).map { appUserRoleRows => appUserRowOpt.map { appUserRow =>
      AvyEyesUser(appUserRow.createTime, appUserRow.lastActivityTime, appUserRow.email, appUserRow.passwordHash, roles = appUserRoleRows.toList)
    }}
  }

  def insertUser(newUser: AvyEyesUser): Future[Int] = {
    val now = DateTime.now
    val passwordHash = newUser.profiles.headOption.flatMap(_.passwordInfo.map(_.password))
    db.run(AppUserRows += AppUserTableRow(now, now, newUser.email, passwordHash))
  }

  def changePassword(email: String, newPasswordHash: String): Future[Int] = {
    val pwUpdateQuery = AppUserRows.filter(_.email === email).map(userTable => (userTable.lastActivityTime, userTable.passwordHash))
    db.run(pwUpdateQuery.update((DateTime.now, Some(newPasswordHash))))
  }

  def logActivityTime(email: String): Future[Int] = {
    val lastActivityUpdateQuery = AppUserRows.filter(_.email === email).map(userTable => userTable.lastActivityTime)
    db.run(lastActivityUpdateQuery.update(DateTime.now))
  }
}

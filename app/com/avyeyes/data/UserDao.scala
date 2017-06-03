package com.avyeyes.data

import javax.inject.Inject

import com.avyeyes.model.{AvyEyesUser, AvyEyesUserRole}
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserDao @Inject()(val dbConfigProvider: DatabaseConfigProvider,
                        val appLifecycle: ApplicationLifecycle) extends AvyEyesDatabase {

  import dbConfig.profile.api._

  def findUser(email: String): Future[Option[AvyEyesUser]] = db.run(AppUserRows.filter(_.email === email).result.headOption).flatMap { appUserRowOpt =>
    db.run(AppUserRoleAssignmentRows.filter(_.email === email).result).map { appUserRoleAssignmentRows => appUserRowOpt.map { appUserRow =>
      val userRoles = appUserRoleAssignmentRows.map(roleAssignment => AvyEyesUserRole(roleAssignment.roleName)).toList
      AvyEyesUser(appUserRow.createTime, appUserRow.lastActivityTime, appUserRow.email, appUserRow.passwordHash, profiles = List.empty, roles = userRoles)
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

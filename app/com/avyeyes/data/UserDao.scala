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

  def findUser(userId: String): Future[Option[AvyEyesUser]] = db.run(AppUserRows.filter(user => user.email === userId || user.facebookId === userId).result.headOption).flatMap { appUserRowOpt =>
    val optUserFuture: Option[Future[AvyEyesUser]] = appUserRowOpt.map { appUserRow =>
      appendRoles(AvyEyesUser(appUserRow.createTime, appUserRow.lastActivityTime, appUserRow.email, appUserRow.facebookId, appUserRow.passwordHash))
    }
    optUserFuture match {
      case Some(userFuture) => userFuture.map(Some(_))
      case None => Future.successful(None)
    }
  }

  def allUsers: Future[Seq[AvyEyesUser]] = db.run(AppUserRows.result).flatMap { appUserRows =>
    Future.sequence(appUserRows.map { (appUserRow: AppUserTableRow) =>
      appendRoles(AvyEyesUser(appUserRow.createTime, appUserRow.lastActivityTime, appUserRow.email, appUserRow.facebookId, appUserRow.passwordHash))
    })
  }

  private def appendRoles(avyEyesUser: AvyEyesUser): Future[AvyEyesUser] = {
    db.run(AppUserRoleAssignmentRows.filter(_.email === avyEyesUser.email).result).map { appUserRoleAssignments =>
      val roles = appUserRoleAssignments.map(roleAssignment => AvyEyesUserRole(roleAssignment.roleName))
      avyEyesUser.copy(roles = roles.toList)
    }
  }

  def insertUser(newUser: AvyEyesUser): Future[Int] = {
    val now = DateTime.now
    db.run(AppUserRows += AppUserTableRow(now, now, newUser.email, newUser.facebookId, newUser.passwordHash))
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

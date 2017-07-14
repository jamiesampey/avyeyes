package com.avyeyes.service

import javax.inject.Inject

import com.avyeyes.data.UserDao
import com.avyeyes.model.{AvyEyesUser, AvyEyesUserRole}
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AvyEyesUserService @Inject()(dao: UserDao, logger: Logger) extends UserService[AvyEyesUser] {

  private val tokens = TrieMap[String, MailToken]()

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    logger.debug(s"Finding user by providerId/userId: $providerId/$userId")

    dao.findUser(userId).map( _.map { avyEyesUser =>
      logger.debug(s"Found user ${avyEyesUser.email} in the database")

      BasicProfile(
        providerId = providerId,
        userId = avyEyesUser.email,
        firstName = None,
        lastName = None,
        fullName = None,
        email = Some(avyEyesUser.email),
        avatarUrl = None,
        authMethod = AuthenticationMethod.UserPassword,
        passwordInfo = avyEyesUser.passwordHash.map (pwHash => PasswordInfo("bcrypt", pwHash))
      )
    })
  }

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    logger.debug(s"Finding user by providerId/email: $providerId/$email")
    Future(None)
  }

  override def save(profile: BasicProfile, mode: SaveMode): Future[AvyEyesUser] = {
    val now = DateTime.now
    val userFromProfile = AvyEyesUser(now, now, profile.userId, None, List(profile))

    mode match {
      case SaveMode.SignUp =>
        logger.info(s"New user signup event for userId ${profile.userId}")
        Future.successful(userFromProfile)
      case SaveMode.LoggedIn =>
        logger.info(s"User login event for userId ${profile.userId}")
        dao.logActivityTime(profile.userId)
        dao.findUser(profile.userId).map(_.getOrElse(userFromProfile))
      case SaveMode.PasswordChange =>
        logger.info(s"User password change event for userId ${profile.userId}")
        profile.passwordInfo.map(_.password).foreach(passwordHash => dao.changePassword(profile.userId, passwordHash))
        Future.successful(userFromProfile)
    }
  }

  override def link(current: AvyEyesUser, to: BasicProfile): Future[AvyEyesUser] = {
    logger.info(s"User link event for user ${current.email}")
    Future.successful(current.copy(profiles = to :: current.profiles))
  }

  override def passwordInfoFor(user: AvyEyesUser): Future[Option[PasswordInfo]] = Future.successful {
    logger.info(s"Password info for ${user.email}")
    user.passwordHash.map(passwordHash => PasswordInfo("", passwordHash))
  }

  override def updatePasswordInfo(user: AvyEyesUser, info: PasswordInfo): Future[Option[BasicProfile]] = Future.successful {
    logger.info(s"updatePasswordInfo for user ${user.email}")
    Some(BasicProfile(
      providerId = "idontknow",
      userId = user.email,
      firstName = None,
      lastName = None,
      fullName = None,
      email = Some(user.email),
      avatarUrl = None,
      authMethod = AuthenticationMethod.UserPassword
    ))
  }

  def saveToken(token: MailToken): Future[MailToken] = Future.successful {
    tokens += (token.uuid -> token)
    token
  }

  def findToken(token: String): Future[Option[MailToken]] = Future.successful { tokens.get(token) }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.get(uuid) match {
        case Some(token) =>
          tokens -= uuid
          Some(token)
        case None => None
      }
    }
  }

  def deleteExpiredTokens: Unit = {
    tokens --= tokens.filter(_._2.isExpired).keys
  }
}

object AvyEyesUserService {
  val SiteOwnerRole = AvyEyesUserRole("site_owner")
  val AdminRole = AvyEyesUserRole("admin")

  val AdminRoles: List[AvyEyesUserRole] = List(SiteOwnerRole, AdminRole)
}
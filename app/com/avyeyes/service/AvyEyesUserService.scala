package com.avyeyes.service

import javax.inject.Inject

import com.avyeyes.data.UserDao
import com.avyeyes.model.{AvyEyesUser, AvyEyesUserRole}
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core._
import securesocial.core.providers.{FacebookProvider, MailToken, UsernamePasswordProvider}
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AvyEyesUserService @Inject()(userDao: UserDao, logger: Logger) extends UserService[AvyEyesUser] {

  private var tokens = Map[String, MailToken]()

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    logger.debug(s"finding user by userId: $providerId/$userId")
    val futureOptUser = if (providerId == FacebookProvider.Facebook) userDao.findUserByFacebook(userId) else userDao.findUser(userId)
    futureOptUser.map { (userOpt: Option[AvyEyesUser]) =>
      val profileOpt: Option[BasicProfile] = userOpt.flatMap(_.profiles.find(profile => profile.providerId == providerId && profile.userId == userId))
      logger.debug(profileOpt.map(profile => s"Found profile ${profile.providerId}/${profile.userId}").getOrElse(s"No profile found"))
      profileOpt
    }
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    logger.debug(s"finding user by email: $providerId/$email")
    userDao.findUser(email).map( _.flatMap(_.profiles.find(profile => profile.providerId == providerId && profile.userId == email)) )
  }

//  private def findProfile(p: BasicProfile) = {
//    users.find {
//      case (key, value) if value.profiles.exists(su => su.providerId == p.providerId && su.userId == p.userId) => true
//      case _ => false
//    }
//  }
//
//  private def updateProfile(user: BasicProfile, entry: ((String, String), AvyEyesUser)): Future[AvyEyesUser] = {
//    val profiles = entry._2.profiles
//    val updatedList = profiles.patch(profiles.indexWhere(i => i.providerId == user.providerId && i.userId == user.userId), Seq(user), 1)
//    val updatedUser = entry._2.copy(profiles = updatedList)
//    users = users + (entry._1 -> updatedUser)
//    Future.successful(updatedUser)
//  }

  def save(profile: BasicProfile, mode: SaveMode): Future[AvyEyesUser] = {
    val now = DateTime.now
    val newUser = AvyEyesUser(now, now, profile.userId, List(profile))

    mode match {
      case SaveMode.SignUp =>
        logger.debug(s"SignUp save for profile ${profile.providerId}/${profile.userId}")
        Future.successful(newUser)
      case SaveMode.LoggedIn =>
        logger.debug(s"Login save for profile ${profile.providerId}/${profile.userId}")
        userDao.findUser(profile.userId).map {
          case Some(existingUser) => existingUser.copy(profiles = profile :: existingUser.profiles)
          case None => newUser
        }
      case SaveMode.PasswordChange =>
        logger.debug(s"PW change save for profile ${profile.providerId}/${profile.userId}")
        ???
    }
  }

  def link(current: AvyEyesUser, to: BasicProfile): Future[AvyEyesUser] = {
    logger.debug(s"Linking profile ${to.providerId}/${to.userId} to existing user ${current.email}")
    Future.successful {
      if (current.profiles.exists(i => i.providerId == to.providerId && i.userId == to.userId)) current
      else current.copy(profiles = to :: current.profiles)
    }
  }

  def saveToken(token: MailToken): Future[MailToken] = Future.successful {
    tokens += (token.uuid -> token)
    token
  }

  def findToken(token: String): Future[Option[MailToken]] = Future.successful(tokens.get(token))

  def deleteToken(uuid: String): Future[Option[MailToken]] = Future.successful {
    tokens.get(uuid) match {
      case Some(token) =>
        tokens -= uuid
        Some(token)
      case None => None
    }
  }

  def deleteExpiredTokens: Unit = tokens = tokens.filter(!_._2.isExpired)

  override def updatePasswordInfo(user: AvyEyesUser, info: PasswordInfo): Future[Option[BasicProfile]] = {
    logger.debug(s"Updating PW info for ${user.email} (new PW ${info.password})")
    userDao.changePassword(user.email, info.password).flatMap { _ =>
      userDao.findUser(user.email).map((userOpt: Option[AvyEyesUser]) => userOpt.flatMap(_.profiles.headOption))
    }
  }

  override def passwordInfoFor(user: AvyEyesUser): Future[Option[PasswordInfo]] = {
    logger.debug(s"Retrieving PW info for ${user.email}")
    Future.successful(user.profiles.find(_.providerId == UsernamePasswordProvider.UsernamePassword).flatMap(_.passwordInfo))
  }
}

object AvyEyesUserService {
  val SiteOwnerRole = AvyEyesUserRole("site_owner")
  val AdminRole = AvyEyesUserRole("admin")

  val AdminRoles: List[AvyEyesUserRole] = List(SiteOwnerRole, AdminRole)
}

package com.jamiesampey.avyeyes.service

import javax.inject.{Inject, Singleton}

import com.jamiesampey.avyeyes.data.UserDao
import com.jamiesampey.avyeyes.model.{AvyEyesUser, AvyEyesUserRole}
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core._
import securesocial.core.providers.{FacebookProvider, MailToken, UsernamePasswordProvider}
import securesocial.core.services.{SaveMode, UserService}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AvyEyesUserService @Inject()(userDao: UserDao, logger: Logger) extends UserService[AvyEyesUser] {

  private val userProfilesMap: Future[MutableMap[AvyEyesUser, Set[BasicProfile]]] = userDao.allUsers.map { avyEyesUsers =>
    val trieMap = new TrieMap[AvyEyesUser, Set[BasicProfile]]()
    avyEyesUsers.foreach(avyEyesUser => trieMap += avyEyesUser -> initProfilesForUser(avyEyesUser))
    trieMap
  }

  private var tokens = Map[String, MailToken]()

  private def initProfilesForUser(avyEyesUser: AvyEyesUser): Set[BasicProfile] = {
    val userPassProfile = avyEyesUser.passwordHash.map { pwHash =>
      BasicProfile(
        providerId = UsernamePasswordProvider.UsernamePassword,
        userId = avyEyesUser.email,
        firstName = None,
        lastName = None,
        fullName = None,
        email = Some(avyEyesUser.email),
        avatarUrl = None,
        authMethod = AuthenticationMethod.UserPassword,
        passwordInfo = Some(PasswordInfo("bcrypt", pwHash))
      )
    }

    val fbProfile = avyEyesUser.facebookId.map { fbId =>
      BasicProfile(
        providerId = FacebookProvider.Facebook,
        userId = fbId,
        firstName = None,
        lastName = None,
        fullName = None,
        email = Some(avyEyesUser.email),
        avatarUrl = None,
        authMethod = AuthenticationMethod.OAuth2
      )
    }

    Set(userPassProfile, fbProfile).flatten
  }

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    logger.debug(s"finding user by userId: $providerId/$userId")
    findProfile(providerId, userId)
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    logger.debug(s"finding user by email: $providerId/$email")
    findProfile(providerId, email)
  }

  private def findProfile(providerId: String, userId: String) = userProfilesMap.map { map =>
    val profileOpt = map.flatMap(_._2).find(profile => profile.providerId == providerId && profile.userId == userId)
    logger.debug(profileOpt.map(profile => s"Found matching profile for ${profile.providerId}/${profile.userId}").getOrElse(s"No matching profile found"))
    profileOpt
  }

  def save(updatedProfile: BasicProfile, mode: SaveMode): Future[AvyEyesUser] = mode match {
    case SaveMode.SignUp =>
      logger.debug(s"SignUp save for profile ${updatedProfile.providerId}/${updatedProfile.userId}")
      val now = DateTime.now
      Future.successful(AvyEyesUser(now, now, updatedProfile.userId, None, None))
    case SaveMode.LoggedIn =>
      logger.debug(s"Login save for profile ${updatedProfile.providerId}/${updatedProfile.userId}")
      userProfilesMap.map { userMap =>
        val userEntryOpt = userMap.find(entry => entry._2.exists(profile => profile.providerId == updatedProfile.providerId && profile.userId == updatedProfile.userId))
        userEntryOpt.map { userEntry =>
          logger.debug(s"found existing user ${userEntry._1.email} with matching profile. Updating user's profiles")
          val newUserProfiles = userEntry._2.filterNot(p => p.providerId == updatedProfile.providerId && p.userId == updatedProfile.userId) + updatedProfile
          userMap += userEntry._1 -> newUserProfiles
          logger.debug(s"Updated user's profiles to $newUserProfiles")
          userEntry._1
        }.getOrElse(throw new RuntimeException(s"logged in a non-existent profile!"))
      }
    case SaveMode.PasswordChange =>
      logger.debug(s"PW change save for profile ${updatedProfile.providerId}/${updatedProfile.userId}")
      ???
  }

  def link(current: AvyEyesUser, newProfile: BasicProfile): Future[AvyEyesUser] = {
    logger.debug(s"Linking profile ${newProfile.providerId}/${newProfile.userId} to existing user ${current.email}")
    userProfilesMap.map { userMap =>
      userMap.get(current).map { existingProfiles =>
        userMap += current -> (existingProfiles + newProfile)
        current
      }.getOrElse(throw new RuntimeException(s"Attempting to link profile to a non-existent user!"))
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

  def updatePasswordInfo(user: AvyEyesUser, info: PasswordInfo): Future[Option[BasicProfile]] = {
    logger.debug(s"Updating PW info for ${user.email} (not really)")
    userProfilesMap.map(_.get(user).flatMap(_.find(p => p.providerId == UsernamePasswordProvider.UsernamePassword)))
  }

  def passwordInfoFor(user: AvyEyesUser): Future[Option[PasswordInfo]] = {
    logger.debug(s"Retrieving password info for ${user.email}")
    userProfilesMap.map { _.get(user).flatMap { profiles =>
      profiles.find(p => p.providerId == UsernamePasswordProvider.UsernamePassword).flatMap(_.passwordInfo)
    }}
  }
}

object AvyEyesUserService {
  val SiteOwnerRole = AvyEyesUserRole("site_owner")
  val AdminRole = AvyEyesUserRole("admin")

  val AdminRoles: List[AvyEyesUserRole] = List(SiteOwnerRole, AdminRole)
}

package com.avyeyes.service

import javax.inject.Inject

import com.avyeyes.data.UserDao
import com.avyeyes.model.AvyEyesUser
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core.{BasicProfile, PasswordInfo}
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.Future

class AvyEyesUserService @Inject()(dao: UserDao, logger: Logger) extends UserService[AvyEyesUser] {

  private var tokens = Map[String, MailToken]()

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = ???
//  {
//    if (logger.isDebugEnabled) {
//      logger.debug("users = %s".format(users))
//    }
//    val result = for (
//      user <- users.values;
//      basicProfile <- user.identities.find(su => su.providerId == providerId && su.userId == userId)
//    ) yield {
//      basicProfile
//    }
//    Future.successful(result.headOption)
//  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = ???
  //  {
//    if (logger.isDebugEnabled) {
//      logger.debug("users = %s".format(users))
//    }
//    val someEmail = Some(email)
//    val result = for (
//      user <- users.values;
//      basicProfile <- user.identities.find(su => su.providerId == providerId && su.email == someEmail)
//    ) yield {
//      basicProfile
//    }
//    Future.successful(result.headOption)
//  }

//  private def findProfile(p: BasicProfile) = {
//    users.find {
//      case (key, value) if value.identities.exists(su => su.providerId == p.providerId && su.userId == p.userId) => true
//      case _ => false
//    }
//  }

//  private def updateProfile(user: BasicProfile, entry: ((String, String), DemoUser)): Future[DemoUser] = {
//    val identities = entry._2.identities
//    val updatedList = identities.patch(identities.indexWhere(i => i.providerId == user.providerId && i.userId == user.userId), Seq(user), 1)
//    val updatedUser = entry._2.copy(identities = updatedList)
//    users = users + (entry._1 -> updatedUser)
//    Future.successful(updatedUser)
//  }

  def save(profile: BasicProfile, mode: SaveMode): Future[AvyEyesUser] = ???
//  mode match {
//    case SaveMode.SignUp =>
//      val now = DateTime.now
//      val newUser = AvyEyesUser(now, now, profile.userId, None, List(profile))
//      dao.insertUser(newUser)
//      Future.successful(newUser)
//    case SaveMode.LoggedIn =>
//      // first see if there is a user with this BasicProfile already.
//      findProfile(profile) match {
//        case Some(existingUser) =>
//          updateProfile(profile, existingUser)
//
//        case None =>
//          val newUser = DemoUser(profile, List(profile))
//          users = users + ((profile.providerId, profile.userId) -> newUser)
//          Future.successful(newUser)
//      }
//
//    case SaveMode.PasswordChange =>
//      findProfile(profile).map { entry => updateProfile(profile, entry) }.getOrElse(
//        // this should not happen as the profile will be there
//        throw new Exception("missing profile)")
//      )
//    }
//  }

  override def link(current: AvyEyesUser, to: BasicProfile): Future[AvyEyesUser] = ???
//  {
//    if (current.identities.exists(i => i.providerId == to.providerId && i.userId == to.userId)) {
//      Future.successful(current)
//    } else {
//      val added = to :: current.identities
//      val updatedUser = current.copy(identities = added)
//      users = users + ((current.main.providerId, current.main.userId) -> updatedUser)
//      Future.successful(updatedUser)
//    }
//  }

  def saveToken(token: MailToken): Future[MailToken] = {
    Future.successful {
      tokens += (token.uuid -> token)
      token
    }
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    Future.successful { tokens.get(token) }
  }

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

  //  def deleteTokens(): Future {
  //    tokens = Map()
  //  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }

  override def updatePasswordInfo(user: AvyEyesUser, info: PasswordInfo): Future[Option[BasicProfile]] = ???
//  {
//    Future.successful {
//      for (
//        found <- users.values.find(_ == user);
//        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
//      ) yield {
//        val idx = found.identities.indexOf(identityWithPasswordInfo)
//        val updated = identityWithPasswordInfo.copy(passwordInfo = Some(info))
//        val updatedIdentities = found.identities.patch(idx, Seq(updated), 1)
//        val updatedEntry = found.copy(identities = updatedIdentities)
//        users = users + ((updatedEntry.main.providerId, updatedEntry.main.userId) -> updatedEntry)
//        updated
//      }
//    }
//  }

  override def passwordInfoFor(user: AvyEyesUser): Future[Option[PasswordInfo]] = ???
//  {
//    Future.successful {
//      for (
//        found <- users.values.find(u => u.main.providerId == user.main.providerId && u.main.userId == user.main.userId);
//        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
//      ) yield {
//        identityWithPasswordInfo.passwordInfo.get
//      }
//    }
//  }
}

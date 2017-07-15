package com.avyeyes.service

import com.avyeyes.data.UserDao
import com.avyeyes.model.AvyEyesUser
import helpers.BaseSpec
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}

import scala.concurrent.Future

class AvyEyesUserServiceTest extends BaseSpec {

  val adminUserEmail = "mradmin@avyeyes.com"

  val adminUserPassProfile = BasicProfile(
    providerId = UsernamePasswordProvider.UsernamePassword,
    userId = adminUserEmail,
    firstName = None,
    lastName = None,
    fullName = None,
    email = Some(adminUserEmail),
    avatarUrl = None,
    authMethod = AuthenticationMethod.UserPassword,
    passwordInfo = Some(PasswordInfo("bcrypt", "9tijdj90w9gjdg0je0-i0-iegjop"))
  )

  val adminUser = AvyEyesUser(
    createTime = DateTime.now.minusYears(1),
    lastActivityTime = DateTime.now.minusHours(1),
    email = adminUserEmail,
    profiles = List(adminUserPassProfile)
  )

  val mockUserDao = mock[UserDao]
  mockUserDao.findUser(adminUser.email) returns Future.successful(Some(adminUser))

  val subject = new AvyEyesUserService(mockUserDao, mock[Logger])

  "AvyEyesUserService" should {
    "Find a user by user ID" in {
      val userProfile = subject.find(UsernamePasswordProvider.UsernamePassword, adminUser.email).resolve.get

      userProfile.userId mustEqual adminUser.email
      userProfile.providerId mustEqual UsernamePasswordProvider.UsernamePassword
      userProfile.email must beSome(adminUser.email)
      userProfile.authMethod mustEqual AuthenticationMethod.UserPassword
      userProfile.passwordInfo mustEqual adminUserPassProfile.passwordInfo
    }
  }
}
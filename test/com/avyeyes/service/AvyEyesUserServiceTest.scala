package com.avyeyes.service

import com.avyeyes.data.UserDao
import com.avyeyes.model.AvyEyesUser
import helpers.BaseSpec
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core.{AuthenticationMethod, PasswordInfo}
import securesocial.core.providers.{FacebookProvider, UsernamePasswordProvider}

import scala.concurrent.Future

class AvyEyesUserServiceTest extends BaseSpec {

  val adminUser = AvyEyesUser(
    createTime = DateTime.now.minusYears(1),
    lastActivityTime = DateTime.now.minusHours(1),
    email = "mradmin@avyeyes.com",
    passwordHash = Some("9tijdj90w9gjdg0je0-i0-iegjop"),
    roles = List(AvyEyesUserService.AdminRole)
  )

  val mockUserDao = mock[UserDao]
  mockUserDao.findUser(adminUser.email) returns Future.successful(Some(adminUser))

  val subject = new AvyEyesUserService(mockUserDao, mock[Logger])

  "AvyEyesUserService" should {
    "Find a user by user ID" in {
      val userProfile = subject.find(FacebookProvider.Facebook, adminUser.email).resolve.get

      userProfile.userId mustEqual adminUser.email
      userProfile.providerId mustEqual FacebookProvider.Facebook
      userProfile.email must beSome(adminUser.email)
      userProfile.authMethod mustEqual AuthenticationMethod.OAuth2
      userProfile.passwordInfo must beSome(PasswordInfo("bcrypt", adminUser.passwordHash.get, None))
    }

    "Find a user by email and provider ID" in {
      val userProfile = subject.findByEmailAndProvider(adminUser.email, UsernamePasswordProvider.UsernamePassword).resolve.get

      userProfile.userId mustEqual adminUser.email
      userProfile.providerId mustEqual UsernamePasswordProvider.UsernamePassword
      userProfile.email must beSome(adminUser.email)
      userProfile.authMethod mustEqual AuthenticationMethod.UserPassword
      userProfile.passwordInfo must beSome(PasswordInfo("bcrypt", adminUser.passwordHash.get, None))
    }
  }
}
package com.jamiesampey.avyeyes.service

import com.jamiesampey.avyeyes.data.UserDao
import com.jamiesampey.avyeyes.model.AvyEyesUser
import helpers.BaseSpec
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core.AuthenticationMethod
import securesocial.core.providers.UsernamePasswordProvider

import scala.concurrent.Future

class AvyEyesUserServiceTest extends BaseSpec {

  val adminUser = AvyEyesUser(
    createTime = DateTime.now.minusYears(1),
    lastActivityTime = DateTime.now.minusHours(1),
    email = "mradmin@avyeyes.com",
    facebookId = Some("49582903490585920"),
    passwordHash = Some("9tijdj90w9gjdg0je0-i0-iegjop"),
    roles = List(AvyEyesUserService.AdminRole)
  )

  val mockUserDao = mock[UserDao]
  mockUserDao.allUsers returns Future.successful(Seq(adminUser))

  val subject = new AvyEyesUserService(mockUserDao, mock[Logger])

  "AvyEyesUserService" should {
    "Find a user by user ID" in {
      val userProfile = subject.find(UsernamePasswordProvider.UsernamePassword, adminUser.email).resolve.get

      userProfile.userId mustEqual adminUser.email
      userProfile.providerId mustEqual UsernamePasswordProvider.UsernamePassword
      userProfile.email must beSome(adminUser.email)
      userProfile.authMethod mustEqual AuthenticationMethod.UserPassword
      userProfile.passwordInfo.map(_.password) mustEqual adminUser.passwordHash
    }
  }
}
package com.avyeyes.data

import org.joda.time.{DateTime, Seconds}
import play.api.test.WithApplication
import securesocial.core.providers.UsernamePasswordProvider

class UserDaoTest extends DatabaseTest {

  implicit val subject = injector.instanceOf[UserDao]

  val testUser = genAvyEyesUser.generate

  "User DAO" should {

    "insert and retrieve a user" in new WithApplication(appBuilder.build) {
      subject.insertUser(testUser).resolve
      val user = subject.findUser(testUser.email).resolve.get

      user.email mustEqual testUser.email
      user.createTime mustEqual user.lastActivityTime
      Seconds.secondsBetween(user.lastActivityTime, DateTime.now).getSeconds must beLessThan(3)
    }

    "change password" in new WithApplication(appBuilder.build) {
      subject.insertUser(testUser).resolve
      val newPasswordHash = "94ijosvo9w9SDOf90SFD()_ios"
      Thread.sleep(1100)
      subject.changePassword(testUser.email, newPasswordHash)

      val user = subject.findUser(testUser.email).resolve.get
      val updatedUserPassProfile = user.profiles.find(_.providerId == UsernamePasswordProvider.UsernamePassword).get
      updatedUserPassProfile.passwordInfo.map(_.password) must beSome(newPasswordHash)
    }

    "log last activity time" in new WithApplication(appBuilder.build) {
      subject.insertUser(testUser).resolve
      Thread.sleep(1100)
      subject.logActivityTime(testUser.email).resolve

      val user = subject.findUser(testUser.email).resolve.get

      user.createTime mustNotEqual user.lastActivityTime
      Seconds.secondsBetween(user.createTime, user.lastActivityTime).getSeconds must beGreaterThanOrEqualTo(1)
    }
  }
}

package com.avyeyes.data

import org.joda.time.{DateTime, Seconds}
import play.api.test.WithApplication

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

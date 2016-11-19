package com.avyeyes.service

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.UserRole
import com.avyeyes.util.Constants._
import com.avyeyes.util.FutureOps._
import com.avyeyes.test.Generators._
import net.liftweb.common.{Full, Empty, Box}
import org.joda.time.DateTime
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.AroundExample

import scala.concurrent.Future

class UserSessionTest extends Specification with AroundExample with Mockito {
  val mockResources = mock[ResourceService]
  val mockDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.dal.doWith(mockDal) {
        AsResult(t)
      }
    }

  class UserSessionForTest(isAdmin: Boolean = false) extends UserSession {
    override def isAdminSession = isAdmin

    var sessionVarCapture: Box[String] = Empty
    override def setSessionVar(emailBox: Box[String]) = {
      sessionVarCapture = emailBox
      sessionVarCapture
    }
  }

  val testSiteOwnerUserRole = UserRole("john.adams@here.com", SiteOwnerRole)
  val testAdminUserRole = UserRole("thomas.jefferson@here.com", AdminRole)

  "Logging in and out" >> {
    isolated

    "Logs authorized admin user in" in {
      mockDal.userRoles(testAdminUserRole.email) returns Future.successful(Seq(testAdminUserRole))

      val userSession = new UserSessionForTest
      userSession.attemptLogin(testAdminUserRole.email)

      userSession.sessionVarCapture mustEqual Full(testAdminUserRole.email)
    }

    "Logs authorized site owner user in" in {
      mockDal.userRoles(testSiteOwnerUserRole.email) returns Future.successful(Seq(testSiteOwnerUserRole))

      val userSession = new UserSessionForTest
      userSession.attemptLogin(testSiteOwnerUserRole.email)

      userSession.sessionVarCapture mustEqual Full(testSiteOwnerUserRole.email)
    }

    "Denies login to unauthorized user" in {
      mockDal.userRoles(any[String]) returns Future.successful(Nil)
      val userSession = new UserSessionForTest
      userSession.attemptLogin("intruder@here.com")

      userSession.sessionVarCapture mustEqual Empty
    }

    "Logs user out" in {
      val userSession = new UserSessionForTest
      userSession.sessionVarCapture = Full(testAdminUserRole.email)
      userSession.logout

      userSession.sessionVarCapture mustEqual Empty
    }
  }

  "Avalanche read-only access" >> {

    "Allows read-only access if the avalanche is viewable" in {
      val userSession = new UserSessionForTest
      val viewableAvalanche = avalancheForTest.copy(viewable = true)
      userSession.isAuthorizedToViewAvalanche(viewableAvalanche) must beTrue
    }

    "Allows admin read-only access" in {
      val userSession = new UserSessionForTest(true)
      val unviewableAvalanche = avalancheForTest.copy(viewable = false)
      userSession.isAuthorizedToViewAvalanche(unviewableAvalanche) must beTrue
    }

    "Otherwise denies read-only access" in {
      val userSession = new UserSessionForTest
      val viewableAvalanche = avalancheForTest.copy(viewable = false)
      userSession.isAuthorizedToViewAvalanche(viewableAvalanche) must beFalse
    }
  }

  "Avalanche read-write access" >> {

    "Allow read-write access for an admin user" in {
      val testAvalanche = avalancheForTest.copy(viewable = false)
      val userSession = new UserSessionForTest(true)
      userSession.isAuthorizedToEditAvalanche(testAvalanche, Empty) must beTrue
    }

    "Allow read-write access with the edit key within the edit window" in {
      val testAvalanche = avalancheForTest.copy(createTime = DateTime.now.minusDays(1))
      val userSession = new UserSessionForTest
      userSession.isAuthorizedToEditAvalanche(testAvalanche, Full(testAvalanche.editKey.toString))
    }

    "Allow read-write access if reservation exists" in {
      val testAvalanche = avalancheForTest
      val userSession = new UserSessionForTest {
        override def reservationExists(extId: String) = extId == testAvalanche.extId
      }
      userSession.isAuthorizedToEditAvalanche(testAvalanche, Empty) must beTrue
    }

    "Otherwise deny read-write access" in {
      val userSession = new UserSessionForTest
      userSession.isAuthorizedToEditAvalanche(avalancheForTest, Empty) must beFalse
    }
  }
}
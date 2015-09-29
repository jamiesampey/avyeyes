package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.service.{UserSession, Injectors, ResourceService}
import com.avyeyes.test._
import com.avyeyes.test.LiftHelpers._
import net.liftweb.common.Empty
import net.liftweb.http.{RedirectResponse, S}
import omniauth._
import omniauth.lib._
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

class OmniAuthCallbackTest extends WebSpec2 with AroundExample with Mockito {
  isolated

  val mockResources = mock[ResourceService]
  val mockUserSession = mock[UserSession]
  val mockAvalancheDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.user.doWith(mockUserSession) {
        Injectors.dal.doWith(mockAvalancheDal) {
          AsResult(t)
        }
      }
    }

  "Omniauth success endpoint" >> {
    "Not attempt login if authinfo email is missing" withSFor "https://avyeyes.com/auth/omnisuccess" in {
      Omniauth.setAuthInfo(makeTestAuthInfo("google.com", "john", None))
      val omniAuthCallback = new OmniAuthCallback

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(omniAuthCallback(req)())

      there was no(mockUserSession).attemptLogin(anyString)

      resp.asInstanceOf[RedirectResponse].uri must_== omniAuthCallback.RedirectUri
      Omniauth.currentAuth must_== Empty
    }

    "Extract the authInfo email if present and attempt login" withSFor "https://avyeyes.com/auth/omnisuccess" in {
      val testEmail = "john.adams@gmail.com"
      Omniauth.setAuthInfo(makeTestAuthInfo("google.com", "john", Some(testEmail)))
      val omniAuthCallback = new OmniAuthCallback

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(omniAuthCallback(req)())

      val emailArgCapture = capture[String]
      there was one(mockUserSession).attemptLogin(emailArgCapture)

      resp.asInstanceOf[RedirectResponse].uri must_== omniAuthCallback.RedirectUri
      emailArgCapture.value must_== testEmail
      Omniauth.currentAuth must_== Empty
    }
  }

  "Omniauth failure endpoint" >> {
    "Redirect back to login" withSFor "https://avyeyes.com/auth/omnifailure" in {
      Omniauth.setAuthInfo(makeTestAuthInfo("google.com", "john", Some("thomas.jefferson@yahoo.com")))
      val omniAuthCallback = new OmniAuthCallback

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(omniAuthCallback(req)())

      resp.asInstanceOf[RedirectResponse].uri must_== omniAuthCallback.RedirectUri
      Omniauth.currentAuth must_== Empty
    }
  }

  private def makeTestAuthInfo(provider: String, name: String, emailOpt: Option[String]) = {
    AuthInfo(provider, "1234", name, AuthToken("testToken", None, None, None), email = emailOpt)
  }
}

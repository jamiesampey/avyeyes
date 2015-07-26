package com.avyeyes.test

import com.avyeyes.data.{DaoInjector, CachedDao}
import com.avyeyes.service.UserInjector
import com.avyeyes.util.UserSession
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._


trait MockInjectors extends AroundExample with Mockito {
  val mockUserSession = mock[UserSession]
  val mockAvalancheDao = mock[CachedDao]

  def around[T: AsResult](t: => T): Result = {
    UserInjector.userSession.doWith(mockUserSession) {
      DaoInjector.dao.doWith(mockAvalancheDao) {
        AsResult(t)
      }
    }
  }
}
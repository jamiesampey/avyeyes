package com.avyeyes.test

import com.avyeyes.data.{DalInjector, CachedDAL}
import com.avyeyes.service.UserInjector
import com.avyeyes.util.UserSession
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._


trait MockInjectors extends AroundExample with Mockito {
  protected lazy val mockUserSession = mock[UserSession]
  protected lazy val mockAvalancheDal = mock[CachedDAL]

  def around[T: AsResult](t: => T): Result = UserInjector.userSession.doWith(mockUserSession) {
    DalInjector.dal.doWith(mockAvalancheDal) {
      AsResult(t)
    }
  }
}
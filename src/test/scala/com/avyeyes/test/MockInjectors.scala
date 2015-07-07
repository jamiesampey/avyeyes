package com.avyeyes.test

import com.avyeyes.database.{DaoInjector, AvalancheDao}
import com.avyeyes.service.UserInjector
import com.avyeyes.util.UserSession
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._


trait MockInjectors extends AroundExample with Mockito {
  val mockUserSession = mock[UserSession]
  val mockAvalancheDao = mock[AvalancheDao]

  SessionFactory.concreteFactory = Some(() => Session.create(mock[java.sql.Connection], mock[PostgreSqlAdapter]))
  
  def around[T: AsResult](t: => T): Result = {
    UserInjector.userSession.doWith(mockUserSession) {
      DaoInjector.avalancheDao.doWith(mockAvalancheDao) {
        AsResult(t)
      }
    }
  }
}
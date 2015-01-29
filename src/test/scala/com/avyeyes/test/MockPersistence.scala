package com.avyeyes.test

import com.avyeyes.persist._
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{Session, SessionFactory}


trait MockPersistence extends AroundExample with Mockito {
  val mockAvalancheDao = mock[AvalancheDao]

  SessionFactory.concreteFactory = Some(() => Session.create(mock[java.sql.Connection], mock[PostgreSqlAdapter]))
  
  def around[T: AsResult](t: => T): Result = {
    DaoInjector.avalancheDao.doWith(mockAvalancheDao) {
      AsResult(t)
    }
  }
}
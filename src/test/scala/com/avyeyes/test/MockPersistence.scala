package com.avyeyes.test

import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.PostgreSqlAdapter
import com.avyeyes.persist._


trait MockPersistence extends BeforeExample with AroundExample with Mockito {
  val mockAvalancheDao = mock[AvalancheDao]
  
  def before() = {
    SessionFactory.concreteFactory = Some(()=>
      Session.create(mock[java.sql.Connection], mock[PostgreSqlAdapter]))
  }
  
  def around[T: AsResult](t: => T): Result = {
    PersistenceInjector.avalancheDao.doWith(mockAvalancheDao) {
      AsResult(t)
    }
  }
}
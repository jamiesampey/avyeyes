package com.avyeyes.test

import scala.xml.XML

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.PostgreSqlAdapter

import com.avyeyes.persist._

import bootstrap.liftweb.Boot

abstract class AvyEyesSpec extends WebSpec2(Boot().boot _) with AroundExample with Mockito {
  lazy val IndexHtmlElem = XML.loadFile("src/main/webapp/index.html")
  lazy val WhaWhaHtmlElem = XML.loadFile("src/main/webapp/whawha.html")
  
  def p(str: String) = System.out.println(str)

  val mockAvalancheDao = mock[AvalancheDao]
  
  def initMockAvalancheDao: AvalancheDao = {
    SessionFactory.concreteFactory = Some(()=>
      Session.create(mock[java.sql.Connection], mock[PostgreSqlAdapter]))
    mockAvalancheDao
  }
  
  def around[T: AsResult](t: => T): Result = {
    PersistenceInjector.avalancheDao.doWith(initMockAvalancheDao _) {
      AsResult(t)
    }
  }
}

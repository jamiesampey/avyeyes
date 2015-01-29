package com.avyeyes.persist

import java.sql.DriverManager

import com.avyeyes.persist.AvyEyesSqueryl._
import com.avyeyes.util.UserSession
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._
import org.squeryl._
import org.squeryl.adapters.H2Adapter

trait InMemoryDB extends AroundExample with Mockito {
  Class.forName("org.h2.Driver")

  val Authorized = mock[UserSession]
  Authorized.isAuthorizedSession() returns true

  val NotAuthorized = mock[UserSession]
  NotAuthorized.isAuthorizedSession() returns false

  SessionFactory.concreteFactory = Some(() =>
    Session.create(DriverManager.getConnection("jdbc:h2:mem:avyeyes_db_test;DB_CLOSE_DELAY=-1", "sa", ""), 
      new H2Adapter))
  
  def recreateDB() = {
    try {
      transaction {
        AvyEyesSchema.drop
        AvyEyesSchema.create
      }
    } catch {
      case e: Exception => System.out.println(s"Exception while dropping or creating DB: ${e.getMessage}")
    }
  }
  
  def around[T: AsResult](t: => T): Result = {
    recreateDB
    transaction {
      AsResult(t)
    }
  }
}
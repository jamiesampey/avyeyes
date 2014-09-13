package com.avyeyes.persist

import org.specs2.specification._
import org.specs2.execute._
import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import java.sql.DriverManager

trait InMemoryDB extends BeforeExample with AroundExample {
  def before() = {
    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() =>
      Session.create(DriverManager.getConnection("jdbc:h2:mem:avyeyes_db_test;DB_CLOSE_DELAY=-1", "sa", ""), 
        new H2Adapter))
  }
  
  def wipeDB() = {
    try {
      transaction {
        AvalancheSchema.drop
        AvalancheSchema.create
      }
    } catch {
      case e: Exception => System.out.println(s"Exception while dropping or creating DB: ${e.getMessage}")
    }
  }
  
  def around[T: AsResult](t: => T): Result = {
    wipeDB() 
    transaction {
      AsResult(t)
    }
  }
}
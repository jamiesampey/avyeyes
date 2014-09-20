package com.avyeyes.persist

import net.liftweb.http.Factory

object PersistenceInjector extends Factory {
  val avalancheDao = new FactoryMaker[AvalancheDao](new SquerylAvalancheDao) {}
  val userDao = new FactoryMaker[UserDao](new SquerylUserDao) {}
}
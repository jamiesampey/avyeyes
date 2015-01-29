package com.avyeyes.persist

import com.avyeyes.util.UserSession
import net.liftweb.http.Factory

object DaoInjector extends Factory {
  val userDao = new FactoryMaker[UserDao](new SquerylUserDao) {}

  val avalancheDao = new FactoryMaker[AvalancheDao](new SquerylAvalancheDao(new UserSession)) {}
}
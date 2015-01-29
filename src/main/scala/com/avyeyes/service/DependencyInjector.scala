package com.avyeyes.service

import com.avyeyes.persist._
import com.avyeyes.util.UserSession
import net.liftweb.http.Factory

object DependencyInjector extends Factory {
  val userDao = new FactoryMaker[UserDao](new SquerylUserDao) {}

  val avalancheDao = new FactoryMaker[AvalancheDao](new SquerylAvalancheDao(new UserSession)) {}
}
package com.avyeyes.persist

import com.avyeyes.service.UserInjector
import net.liftweb.http.Factory

object DaoInjector extends Factory {
  val userDao = new FactoryMaker[UserDao](new SquerylUserDao) {}

  val avalancheDao = new FactoryMaker[AvalancheDao](new SquerylAvalancheDao(UserInjector.userSession.vend)) {}
}
package com.avyeyes.model

import com.avyeyes.persist.SquerylUserDao
import com.avyeyes.service.UserInjector
import net.liftweb.http.Factory

object DaoInjector extends Factory {
  val userDao = new FactoryMaker[UserDao](new SquerylUserDao) {}

  val avalancheDao = new FactoryMaker[AvalancheDao](new SlickAvalancheDao(UserInjector.userSession.vend)) {}
}
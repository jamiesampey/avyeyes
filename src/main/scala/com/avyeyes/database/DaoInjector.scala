package com.avyeyes.database

import com.avyeyes.persist.SlickUserDao
import com.avyeyes.service.UserInjector
import net.liftweb.http.Factory

object DaoInjector extends Factory {
  val userDao = new FactoryMaker[UserDao](new SlickUserDao) {}

  val avalancheDao = new FactoryMaker[AvalancheDao](new SlickAvalancheDao(UserInjector.userSession.vend)) {}
}
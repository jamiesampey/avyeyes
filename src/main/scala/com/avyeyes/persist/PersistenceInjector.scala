package com.avyeyes.persist

import net.liftweb.http.Factory
import com.avyeyes.snippet.AdminConsole

object PersistenceInjector extends Factory {
  val avalancheDao = new FactoryMaker[AvalancheDao](
    new SquerylAvalancheDao(() => AdminConsole.isAuthorizedSession)) {}
  
  val userDao = new FactoryMaker[UserDao](new SquerylUserDao) {}
}
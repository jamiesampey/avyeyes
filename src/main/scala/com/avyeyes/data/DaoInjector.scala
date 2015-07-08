package com.avyeyes.data

import com.avyeyes.service.UserInjector
import net.liftweb.http.Factory

object DaoInjector extends Factory {
  val inMemoryDao = new FactoryMaker[InMemoryDao](new TrieMapDao(UserInjector.userSession.vend)) {}

  val diskDao = new FactoryMaker[DiskDao](new SlickRelationalDao(UserInjector.userSession.vend)) {}
}
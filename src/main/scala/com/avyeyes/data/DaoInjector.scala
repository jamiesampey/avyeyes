package com.avyeyes.data

import com.avyeyes.service.UserInjector
import net.liftweb.http.Factory

object DaoInjector extends Factory {
  val diskDao = new FactoryMaker[DiskDao](new SlickRelationalDao(UserInjector.userSession.vend)) {}

  val inMemoryDao = new FactoryMaker[InMemoryDao](
    new TrieMapDao(diskDao.vend, UserInjector.userSession.vend)) {}

}
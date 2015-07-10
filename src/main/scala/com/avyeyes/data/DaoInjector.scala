package com.avyeyes.data

import com.avyeyes.service.UserInjector
import net.liftweb.http.Factory

object DaoInjector extends Factory {
  val dao = new FactoryMaker[CachedDao](
    new TrieMapCachedDao(postgresDataSource, UserInjector.userSession.vend)) {}
}
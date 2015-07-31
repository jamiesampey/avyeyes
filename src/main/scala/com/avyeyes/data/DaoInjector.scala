package com.avyeyes.data

import com.avyeyes.service.UserInjector
import net.liftweb.http.Factory
import slick.driver.PostgresDriver

object DaoInjector extends Factory {
  val dao = new FactoryMaker[CachedDAL](
    new MemoryMapCachedDAL(PostgresDriver, postgresDataSource, AllAvalanchesMap)) {}
}
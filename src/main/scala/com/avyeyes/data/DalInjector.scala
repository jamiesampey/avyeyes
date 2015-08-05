package com.avyeyes.data

import net.liftweb.http.Factory
import slick.driver.PostgresDriver

object DalInjector extends Factory {
  val dal = new FactoryMaker[CachedDAL](
    new MemoryMapCachedDAL(PostgresDriver, postgresDataSource, AllAvalanchesMap)) {}
}
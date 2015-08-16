package com.avyeyes.service

import com.avyeyes.data._
import net.liftweb.http.Factory
import slick.driver.PostgresDriver

object Injectors extends Factory {
  val user = new FactoryMaker[UserSession](new UserSession) {}
  val s3 = new FactoryMaker[AmazonS3ImageService](new AmazonS3ImageService) {}
  val dal = new FactoryMaker[CachedDAL](
    new MemoryMapCachedDAL(PostgresDriver, postgresDataSource, AllAvalanchesMap)) {}
}
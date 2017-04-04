package com.avyeyes.service

import com.avyeyes.data._
import net.liftweb.http.Factory
import slick.driver.PostgresDriver

object Injectors extends Factory {
  lazy val resources = new FactoryMaker[ConfigurationService](new ConfigurationService) {}
  lazy val user = new FactoryMaker[UserSession](new UserSession) {}
  lazy val s3 = new FactoryMaker[AmazonS3Service](new AmazonS3Service) {}
  lazy val dal = new FactoryMaker[CachedDAL](
    new MemoryMapCachedDAL(PostgresDriver, PostgresDataSource, AllAvalanchesMap)) {}
}
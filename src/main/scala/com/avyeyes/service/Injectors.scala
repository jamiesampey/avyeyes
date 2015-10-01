package com.avyeyes.service

import com.avyeyes.data._
import net.liftweb.http.Factory
import slick.driver.PostgresDriver

object Injectors extends Factory {
  lazy val resources = new FactoryMaker[ResourceService](new ResourceService) {}
  lazy val user = new FactoryMaker[UserSession](new UserSession) {}
  lazy val s3 = new FactoryMaker[AmazonS3ImageService](new AmazonS3ImageService) {}
  lazy val dal = new FactoryMaker[CachedDAL](
    new MemoryMapCachedDAL(PostgresDriver, PostgresDataSource, AllAvalanchesMap)) {}
}
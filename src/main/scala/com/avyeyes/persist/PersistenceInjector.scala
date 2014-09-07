package com.avyeyes.persist

import net.liftweb.http.Factory

object PersistenceInjector extends Factory {
  val avalancheDao = new FactoryMaker[AvalancheDao](initSquerylAvalancheDao _) {}
  
  private def initSquerylAvalancheDao: AvalancheDao = {
    val squerylDao = new SquerylAvalancheDao
    squerylDao
  }
}
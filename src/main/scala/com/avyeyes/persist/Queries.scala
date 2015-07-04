package com.avyeyes.persist

import com.avyeyes.model.{Users, AvalancheImages, Avalanches}
import slick.driver.PostgresDriver.api._

object Queries {
  val avalanches = TableQuery[Avalanches]
  val avalancheImages = TableQuery[AvalancheImages]
  val users = TableQuery[Users]
}

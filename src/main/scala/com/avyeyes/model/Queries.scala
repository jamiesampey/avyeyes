package com.avyeyes.model

import slick.driver.PostgresDriver.api._

object Queries {
  val avalanches = TableQuery[Avalanches]
  val avalancheImages = TableQuery[AvalancheImages]
  val users = TableQuery[Users]
  val userRoles = TableQuery[UserRoles]
}

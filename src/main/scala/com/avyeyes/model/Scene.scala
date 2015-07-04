package com.avyeyes.model

import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.model.enums.{Precipitation, SkyCoverage}
import slick.driver.PostgresDriver.api._

case class Scene(skyCoverage: SkyCoverage = SkyCoverage.U, 
                 precipitation: Precipitation = Precipitation.U)

case class LiftedScene(skyCoverage: Rep[SkyCoverage],
                       precipitation: Rep[Precipitation])
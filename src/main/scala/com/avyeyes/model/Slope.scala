package com.avyeyes.model

import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect.Aspect
import slick.driver.PostgresDriver.api._


case class Slope(aspect: Aspect = Aspect.N,
                 angle: Int = 0,
                 elevation: Double)

case class LiftedSlope(aspect: Rep[Aspect],
                       angle: Rep[Int],
                       elevation: Rep[Double])
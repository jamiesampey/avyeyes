package com.avyeyes.model

import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.model.enums.{Precipitation, SkyCoverage}

case class Scene(skyCoverage: SkyCoverage = SkyCoverage.U,
                 precipitation: Precipitation = Precipitation.U)

object Scene {
  implicit def toString(s: Scene) = s"${s.skyCoverage.toString}-${s.precipitation.toString}"

  implicit def fromString(str: String) = {
    val arr = str.split("-")
    Scene(
      skyCoverage = SkyCoverage.withName(arr(0)),
      precipitation = Precipitation.withName(arr(1))
    )
  }
}

package com.avyeyes.model

import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.model.enums.{Precipitation, SkyCoverage}

case class Scene(skyCoverage: SkyCoverage = SkyCoverage.U,
                 precipitation: Precipitation = Precipitation.U) {
  override def toString = s"${skyCoverage.toString}-${precipitation.toString}"
}

object Scene {
  def fromString(str: String) = {
    val arr = str.split("-")
    Scene(
      skyCoverage = SkyCoverage.withName(arr(0)),
      precipitation = Precipitation.withName(arr(1))
    )
  }
}

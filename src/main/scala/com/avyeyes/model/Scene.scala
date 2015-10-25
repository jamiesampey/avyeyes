package com.avyeyes.model

import com.avyeyes.model.enums._
import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage


case class Scene(skyCoverage: SkyCoverage, precipitation: Precipitation) {
  override def toString = s"${SkyCoverage.toCode(skyCoverage)}-${Precipitation.toCode(precipitation)}"
}

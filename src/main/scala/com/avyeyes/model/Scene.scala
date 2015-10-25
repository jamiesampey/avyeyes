package com.avyeyes.model

import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.model.StringSerializers.enumValueToCode


case class Scene(skyCoverage: SkyCoverage, precipitation: Precipitation) {
  override def toString = s"${enumValueToCode(skyCoverage)}-${enumValueToCode(precipitation)}"
}

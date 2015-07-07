package com.avyeyes.model

import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.model.enums.{Precipitation, SkyCoverage}

case class Scene(skyCoverage: SkyCoverage = SkyCoverage.U, 
                 precipitation: Precipitation = Precipitation.U)

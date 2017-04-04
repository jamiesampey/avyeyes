package com.avyeyes.model

import com.avyeyes.model.enums.Direction.Direction
import com.avyeyes.model.enums.WindSpeed.WindSpeed

case class Weather(recentSnow: Int, recentWindSpeed: WindSpeed, recentWindDirection: Direction)

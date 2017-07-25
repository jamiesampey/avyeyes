package com.jamiesampey.avyeyes.model

import com.jamiesampey.avyeyes.model.enums.Direction.Direction
import com.jamiesampey.avyeyes.model.enums.WindSpeed.WindSpeed

case class Weather(recentSnow: Int, recentWindSpeed: WindSpeed, recentWindDirection: Direction)

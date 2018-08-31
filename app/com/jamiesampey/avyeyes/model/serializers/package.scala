package com.jamiesampey.avyeyes.model

import org.json4s.DefaultFormats

package object serializers {

  val defaultFormats = new DefaultFormats { }

  val avyeyesFormats = defaultFormats +
    DateTimeSerializer +
    CoordinateSerializer +
    ClassificationSerializer +
    SlopeSerializer +
    WeatherSerializer +
    AvalancheImageSerializer +
    AvalancheSerializer
}

package com.avyeyes.data

import com.avyeyes.model._

private[data] object SlickRowMappers {

  implicit def toAvalancheRow(avalanche: Avalanche): AvalancheTableRow =
    AvalancheTableRow(
      createTime = avalanche.createTime,
      updateTime = avalanche.updateTime,
      extId = avalanche.extId,
      viewable = avalanche.viewable,
      submitterEmail = avalanche.submitterEmail,
      submitterExp = avalanche.submitterExp,
      areaName = avalanche.areaName,
      date = avalanche.date,
      longitude = avalanche.location.longitude,
      latitude = avalanche.location.latitude,
      elevation = avalanche.location.altitude.toInt,
      aspect = avalanche.slope.aspect,
      angle = avalanche.slope.angle,
      perimeter = avalanche.perimeter,
      comments = avalanche.comments
    )

  implicit def toWeatherRow(avalanche: Avalanche): AvalancheWeatherTableRow =
    AvalancheWeatherTableRow(
      avalanche = avalanche.extId,
      recentSnow = avalanche.weather.recentSnow,
      recentWindSpeed = avalanche.weather.recentWindSpeed,
      recentWindDirection = avalanche.weather.recentWindDirection
    )

  implicit def toClassificationRow(avalanche: Avalanche): AvalancheClassificationTableRow =
    AvalancheClassificationTableRow(
      avalanche = avalanche.extId,
      avalancheType = avalanche.classification.avyType,
      trigger = avalanche.classification.trigger,
      triggerCause = avalanche.classification.triggerCause,
      interface = avalanche.classification.interface,
      rSize = avalanche.classification.rSize,
      dSize = avalanche.classification.dSize
    )

  implicit def toHumanRow(avalanche: Avalanche): AvalancheHumanTableRow =
    AvalancheHumanTableRow(
      avalanche = avalanche.extId,
      modeOfTravel = avalanche.humanNumbers.modeOfTravel,
      caught = avalanche.humanNumbers.caught,
      partiallyBuried = avalanche.humanNumbers.partiallyBuried,
      fullyBuried = avalanche.humanNumbers.fullyBuried,
      injured = avalanche.humanNumbers.injured,
      killed = avalanche.humanNumbers.killed
    )

  def avalancheFromData(data: (AvalancheTableRow, AvalancheWeatherTableRow, AvalancheClassificationTableRow, AvalancheHumanTableRow)): Avalanche =
    Avalanche(
      createTime = data._1.createTime,
      updateTime = data._1.updateTime,
      extId = data._1.extId,
      viewable = data._1.viewable,
      submitterEmail = data._1.submitterEmail,
      submitterExp = data._1.submitterExp,
      location = Coordinate(longitude = data._1.longitude, latitude = data._1.latitude, altitude = data._1.elevation),
      date = data._1.date,
      areaName = data._1.areaName,
      weather = Weather(recentSnow = data._2.recentSnow, recentWindSpeed = data._2.recentWindSpeed, recentWindDirection = data._2.recentWindDirection),
      slope = Slope(aspect = data._1.aspect, angle = data._1.angle, elevation = data._1.elevation),
      classification = Classification(avyType = data._3.avalancheType, trigger = data._3.trigger, triggerCause = data._3.triggerCause, interface = data._3.interface, rSize = data._3.rSize, dSize = data._3.dSize),
      humanNumbers = HumanNumbers(modeOfTravel = data._4.modeOfTravel, caught = data._4.caught, partiallyBuried = data._4.partiallyBuried, fullyBuried = data._4.fullyBuried, injured = data._4.injured, killed = data._4.killed),
      perimeter = data._1.perimeter,
      comments = data._1.comments
    )

}

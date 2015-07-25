package com.avyeyes.data

import java.sql.Timestamp

import com.avyeyes.model.StringConverters._
import com.avyeyes.model._
import com.avyeyes.model.enums._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

private[data] object SlickColumnMappers {
  implicit def dateTimeMapper = MappedColumnType.base[DateTime, Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    ts => new DateTime(ts)
  )

  implicit def coordinateMapper = MappedColumnType.base[Coordinate, String](
    coord => coord.toString,
    str => stringToCoordinate(str)
  )

  implicit def sceneMapper = MappedColumnType.base[Scene, String](
    scene => scene.toString,
    str => stringToScene(str)
  )

  implicit def slopeMapper = MappedColumnType.base[Slope, String](
    slope => slope.toString,
    str => stringToSlope(str)
  )

  implicit def classificationMapper = MappedColumnType.base[Classification, String](
    classification => classification.toString,
    str => stringToClassification(str)
  )

  implicit def humanNumbersMapper = MappedColumnType.base[HumanNumbers, String](
    humanNumbers => humanNumbers.toString,
    str => stringToHumanNumbers(str)
  )

  private def enumNameMapper(enum: Enumeration) = MappedColumnType.base[enum.Value, String](
    enum => enum.toString.split(".")(1),
    str => enum.withName(str)
  )

  implicit def aspectMapper = enumNameMapper(Aspect)
  implicit def avalancheInterfaceMapper = enumNameMapper(AvalancheInterface)
  implicit def avalancheTriggerMapper = enumNameMapper(AvalancheTrigger)
  implicit def avalancheTypeMapper = enumNameMapper(AvalancheType)
  implicit def experienceLevelMapper = enumNameMapper(ExperienceLevel)
  implicit def modeOfTravelMapper = enumNameMapper(ModeOfTravel)
  implicit def precipitationMapper = enumNameMapper(Precipitation)
  implicit def skyCoverageMapper = enumNameMapper(SkyCoverage)
}

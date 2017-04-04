package com.avyeyes.data

import java.sql.Timestamp

import com.avyeyes.model._
import com.avyeyes.model.enums._
import org.joda.time.DateTime
import slick.driver.JdbcProfile

private[data] trait SlickColumnMappers {
  val driver: JdbcProfile
  import driver.api._

  implicit def dateTimeMapper = MappedColumnType.base[DateTime, Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    ts => new DateTime(ts)
  )

  implicit def coordinateMapper = MappedColumnType.base[Coordinate, String](
    coord => coord.toString,
    str => Coordinate.fromString(str)
  )

  implicit def coordinateSeqMapper = MappedColumnType.base[Seq[Coordinate], String](
    seq => seq.mkString(" ").trim,
    str => str.split(" ").map(Coordinate.fromString)
  )

  private def enumNameMapper(enum: AutocompleteEnum) = MappedColumnType.base[enum.Value, String](
    enumValue => enum.toCode(enumValue),
    str => enum.fromCode(str)
  )

  implicit def directionMapper = enumNameMapper(Direction)
  implicit def avalancheInterfaceMapper = enumNameMapper(AvalancheInterface)
  implicit def avalancheTriggerMapper = enumNameMapper(AvalancheTrigger)
  implicit def avalancheTriggerModifierMapper = enumNameMapper(AvalancheTriggerModifier)
  implicit def avalancheTypeMapper = enumNameMapper(AvalancheType)
  implicit def experienceLevelMapper = enumNameMapper(ExperienceLevel)
  implicit def modeOfTravelMapper = enumNameMapper(ModeOfTravel)
  implicit def windSpeedMapper = enumNameMapper(WindSpeed)
}

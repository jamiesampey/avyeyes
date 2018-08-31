package com.jamiesampey.avyeyes.data

import java.sql.Timestamp

import com.jamiesampey.avyeyes.model._
import com.jamiesampey.avyeyes.model.enums._
import org.joda.time.DateTime
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

private[data] trait SlickColumnMappers { self: HasDatabaseConfigProvider[JdbcProfile] =>

  import dbConfig.profile.api._

  implicit def dateTimeMapper = MappedColumnType.base[DateTime, Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    ts => new DateTime(ts)
  )

  implicit def coordinateMapper = MappedColumnType.base[Coordinate, String](
    coord => coord.toString,
    str => Coordinate(str)
  )

  implicit def coordinateSeqMapper = MappedColumnType.base[Seq[Coordinate], String](
    seq => seq.mkString(" ").trim,
    str => str.trim.split(" ").map(Coordinate(_))
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
  implicit def windSpeedMapper = enumNameMapper(WindSpeed)
}

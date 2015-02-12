package com.avyeyes.persist

import java.sql.Timestamp

import org.joda.time.DateTime
import org.squeryl.PrimitiveTypeMode
import org.squeryl.dsl._

object AvyEyesSqueryl extends PrimitiveTypeMode {

  implicit val jodaTimeTEF = new NonPrimitiveJdbcMapper[Timestamp, DateTime, TTimestamp](timestampTEF, this) {
    def convertFromJdbc(t: Timestamp) = new DateTime(t)
    def convertToJdbc(t: DateTime) = new Timestamp(t.getMillis)
  }

  implicit def jodaTimeToTE(dt: DateTime) = jodaTimeTEF.create(dt)
}

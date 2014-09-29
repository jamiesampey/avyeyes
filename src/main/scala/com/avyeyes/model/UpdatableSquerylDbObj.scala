package com.avyeyes.model

import java.sql.Timestamp

abstract class UpdatableSquerylDbObj extends SquerylDbObj {
  val updateTime = new Timestamp(System.currentTimeMillis)
}
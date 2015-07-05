package com.avyeyes.model

import org.joda.time.DateTime

abstract class UpdatableDbObject(updateTime: DateTime = DateTime.now) extends BaseDbObject

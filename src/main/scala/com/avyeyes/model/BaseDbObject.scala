package com.avyeyes.model

import org.joda.time.DateTime

abstract class BaseDbObject(id: Long = 0, createTime: DateTime = DateTime.now)

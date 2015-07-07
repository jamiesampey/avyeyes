package com.avyeyes.database

import com.avyeyes.model.OrderDirection

case class AdminAvalancheQuery(
  extId: Option[String] = None,
  areaName: Option[String] = None,
  submitterEmail: Option[String] = None,
  orderBy: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.id, OrderDirection.asc)),
  offset: Int = 0,
  limit: Int = Int.MaxValue)
  extends BaseAvalancheQuery(orderBy, offset, limit)

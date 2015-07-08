package com.avyeyes.data

case class AdminAvalancheQuery(
  extId: Option[String] = None,
  areaName: Option[String] = None,
  submitterEmail: Option[String] = None,
  orderBy: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.createTime, OrderDirection.desc)),
  offset: Int = 0,
  limit: Int = Int.MaxValue)
  extends BaseAvalancheQuery(orderBy, offset, limit)

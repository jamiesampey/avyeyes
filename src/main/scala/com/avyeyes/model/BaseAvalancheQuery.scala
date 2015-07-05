package com.avyeyes.model

abstract class BaseAvalancheQuery(
  orderBy: List[(OrderField.Value, OrderDirection.Value)],
  offset: Int,
  limit: Int)

object OrderField extends Enumeration {
  type OrderField = Value
  val id, createTime, updateTime, extId, viewable, lat, lng, areaName, avyDate, avyType,
    avyTrigger, avyInterface, rSize, dSize, caught, killed, submitterEmail = Value
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val asc, desc = Value
}
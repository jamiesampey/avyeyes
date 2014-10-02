package com.avyeyes.persist

import com.avyeyes.util.AEHelpers._

case class AvalancheQuery(viewable: Option[Boolean], geo: Option[GeoBounds],
  fromDateStr: String, toDateStr: String, avyTypeStr: String, avyTriggerStr: String, 
  rSize: String, dSize: String, numCaught: String, numKilled: String,
  orderBy: OrderBy.Value = OrderBy.Id, orderDirection: OrderDirection.Value = OrderDirection.ASC,
  page: Int = 0, pageLimit: Int = Int.MaxValue)
 
case class GeoBounds(northStr: String, eastStr: String, southStr: String, westStr: String) {
  def northLimit = strToDblOrZero(northStr)
  def eastLimit = strToDblOrZero(eastStr)
  def southLimit = strToDblOrZero(southStr)
  def westLimit = strToDblOrZero(westStr)
}

object OrderBy extends Enumeration {
  type OrderBy = Value
  val Id, CreateTime, UpdateTime, Lat, Lng, AreaName, AvyDate, AvyType, 
    AvyTrigger, AvyInterface, RSize, DSize, Caught, Killed = Value
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val ASC, DESC = Value
}
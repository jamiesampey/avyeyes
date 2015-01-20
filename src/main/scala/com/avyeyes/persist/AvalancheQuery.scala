package com.avyeyes.persist

import com.avyeyes.util.Helpers._
import java.util.Date
import com.avyeyes.model.enums._

object AvalancheQuery {
  val baseQuery = new AvalancheQuery
}

case class AvalancheQuery(viewable: Option[Boolean] = None, geo: Option[GeoBounds] = None,
  fromDate: Option[Date] = None, toDate: Option[Date] = None, 
  avyType: Option[AvalancheType.Value] = None, avyTrigger: Option[AvalancheTrigger.Value] = None, 
  rSize: Option[Double] = None, dSize: Option[Double] = None, 
  numCaught: Option[Int] = None, numKilled: Option[Int] = None,
  orderBy: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.Id, OrderDirection.Asc)),
  offset: Int = 0, limit: Int = Int.MaxValue)

case class GeoBounds(northStr: String, eastStr: String, southStr: String, westStr: String) {
  def northLimit = strToDblOrZero(northStr)
  def eastLimit = strToDblOrZero(eastStr)
  def southLimit = strToDblOrZero(southStr)
  def westLimit = strToDblOrZero(westStr)
}

object OrderField extends Enumeration {
  type OrderField = Value
  val Id, CreateTime, UpdateTime, ExternalId, Viewable, Lat, Lng, AreaName, AvyDate, AvyType,
    AvyTrigger, AvyInterface, RSize, DSize, Caught, Killed, SubmitterEmail = Value
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val Asc, Desc = Value
}
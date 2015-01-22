package com.avyeyes.persist

import java.util.Date

import com.avyeyes.model.enums._
import com.avyeyes.util.Helpers._

object AvalancheQuery {
  val defaultQuery = new AvalancheQuery
}

case class AvalancheQuery(
  viewable: Option[Boolean] = None, geo: Option[GeoBounds] = None,
  fromDate: Option[Date] = None, toDate: Option[Date] = None, 
  avyType: Option[AvalancheType.Value] = None, avyTrigger: Option[AvalancheTrigger.Value] = None, 
  rSize: Option[Double] = None, dSize: Option[Double] = None, 
  numCaught: Option[Int] = None, numKilled: Option[Int] = None,
  orderBy: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.id, OrderDirection.asc)),
  offset: Int = 0, limit: Int = Int.MaxValue) extends BaseAvalancheQuery(orderBy, offset, limit)

case class GeoBounds(northStr: String, eastStr: String, southStr: String, westStr: String) {
  def northLimit = strToDblOrZero(northStr)
  def eastLimit = strToDblOrZero(eastStr)
  def southLimit = strToDblOrZero(southStr)
  def westLimit = strToDblOrZero(westStr)
}
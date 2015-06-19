package com.avyeyes.persist

import com.avyeyes.model.enums._
import org.joda.time.DateTime

case class AvalancheQuery(
  viewable: Option[Boolean] = None, geo: Option[GeoBounds] = None,
  fromDate: Option[DateTime] = None, toDate: Option[DateTime] = None,
  avyType: Option[AvalancheType.Value] = None, avyTrigger: Option[AvalancheTrigger.Value] = None, 
  rSize: Option[Double] = None, dSize: Option[Double] = None, 
  numCaught: Option[Int] = None, numKilled: Option[Int] = None,
  orderBy: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.id, OrderDirection.asc)),
  offset: Int = 0, limit: Int = Int.MaxValue) extends BaseAvalancheQuery(orderBy, offset, limit)

case class GeoBounds(latMax: Double, latMin: Double, lngMax: Double, lngMin: Double)
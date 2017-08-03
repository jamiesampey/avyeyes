package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.data.AvalanchePredicates._
import com.jamiesampey.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.jamiesampey.avyeyes.model.enums.AvalancheType.AvalancheType
import org.joda.time.DateTime

case class AvalancheSpatialQuery(
  viewable: Option[Boolean] = None,
  geoBounds: Option[GeoBounds] = None,
  dateRange: Option[(DateTime, DateTime)] = None,
  aspectRange: Option[(Int, Int)] = None,
  angleRange: Option[(Int, Int)] = None,
  elevationRange: Option[(Int, Int)] = None,
  avyType: Option[AvalancheType] = None,
  trigger: Option[AvalancheTrigger] = None,
  rSize: Option[Double] = None,
  dSize: Option[Double] = None,
  numCaught: Option[Int] = None,
  numKilled: Option[Int] = None,
  order: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.CreateTime, OrderDirection.desc)),
  offset: Int = 0,
  limit: Int = Int.MaxValue)
  extends OrderedAvalancheQuery {

  lazy val predicates = viewablePredicate(viewable) ::
    geoBoundsPredicate(geoBounds) ::
    dateRangePredicate(dateRange) ::
    aspectRangePredicate(aspectRange) ::
    angleRangePredicate(angleRange) ::
    elevationRangePredicate(elevationRange) ::
    avyTypePredicate(avyType) ::
    triggerPredicate(trigger) ::
    rSizePredicate(rSize) ::
    dSizePredicate(dSize) ::
    caughtPredicate(numCaught) ::
    killedPredicate(numKilled) :: Nil

  def toPredicate = predicates.collect({case Some(predicate) => predicate}) match {
    case definedPredicates if definedPredicates.nonEmpty => and(definedPredicates)
    case _ => _ => true
  }
}

case class GeoBounds(lngMax: Double, lngMin: Double, latMax: Double, latMin: Double)


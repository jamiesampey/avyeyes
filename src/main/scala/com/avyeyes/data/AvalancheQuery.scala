package com.avyeyes.data

import com.avyeyes.data.AvalanchePredicates._
import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import org.joda.time.DateTime

case class AvalancheQuery(
  viewable: Option[Boolean] = None,
  geoBounds: Option[GeoBounds] = None,
  fromDate: Option[DateTime] = None,
  toDate: Option[DateTime] = None,
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

  def toPredicate = and(
    viewablePredicate(viewable),
    geoBoundsPredicate(geoBounds),
    fromDatePredicate(fromDate),
    toDatePredicate(toDate),
    avyTypePredicate(avyType),
    triggerPredicate(trigger),
    rSizePredicate(rSize),
    dSizePredicate(dSize),
    caughtPredicate(numCaught),
    killedPredicate(numKilled)
  )
}

case class GeoBounds(lngMax: Double, lngMin: Double, latMax: Double, latMin: Double)


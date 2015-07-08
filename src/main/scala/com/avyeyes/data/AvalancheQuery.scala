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
  orderBy: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.createTime, OrderDirection.desc)),
  offset: Int = 0,
  limit: Int = Int.MaxValue)
  extends BaseAvalancheQuery(orderBy, offset, limit) {

  def toPredicate: Avalanche => Boolean = {
    and(
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

  private def and(predicates: (Avalanche => Boolean)*)(a: Avalanche) = predicates.forall(predicate => predicate(a))
}

case class GeoBounds(latMax: Double, latMin: Double, lngMax: Double, lngMin: Double)


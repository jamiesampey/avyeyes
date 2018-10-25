package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.data.AvalanchePredicates._
import com.jamiesampey.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.jamiesampey.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.jamiesampey.avyeyes.model.enums.AvalancheType.AvalancheType
import org.joda.time.DateTime

case class AvalancheSpatialQuery(
  viewable: Option[Boolean] = None,
  geoBounds: Option[GeoBounds] = None,
  fromDate: Option[DateTime] = None,
  toDate: Option[DateTime] = None,
  avyTypes: Option[Seq[AvalancheType]] = None,
  triggers: Option[Seq[AvalancheTrigger]] = None,
  interfaces: Option[Seq[AvalancheInterface]] = None,
  rSize: Option[Double] = None,
  dSize: Option[Double] = None,
  orderBy: OrderField.Value = OrderField.CreateTime,
  order: OrderDirection.Value = OrderDirection.desc,
  offset: Int = 0,
  limit: Int = Int.MaxValue)
extends OrderedAvalancheQuery {

  lazy val predicates = viewablePredicate(viewable) ::
    geoBoundsPredicate(geoBounds) ::
    fromDatePredicate(fromDate) ::
    toDatePredicate(toDate) ::
    avyTypesPredicate(avyTypes) ::
    triggersPredicate(triggers) ::
    interfacesPredicate(interfaces) ::
    rSizePredicate(rSize) ::
    dSizePredicate(dSize) :: Nil

  def toPredicate = predicates.collect({case Some(predicate) => predicate}) match {
    case definedPredicates if definedPredicates.nonEmpty => and(definedPredicates)
    case _ => _ => true
  }
}

case class GeoBounds(lngMax: Double, lngMin: Double, latMax: Double, latMin: Double)


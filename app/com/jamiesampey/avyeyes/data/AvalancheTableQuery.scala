package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.data.AvalanchePredicates._

case class AvalancheTableQuery(
  extId: Option[String] = None,
  areaName: Option[String] = None,
  submitterEmail: Option[String] = None,
  order: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.CreateTime, OrderDirection.desc)),
  offset: Int = 0,
  limit: Int = Int.MaxValue)
  extends OrderedAvalancheQuery {

  lazy val predicates =
    extIdPredicate(extId) ::
    areaNamePredicate(areaName) ::
    emailPredicate(submitterEmail) :: Nil

  def toPredicate = predicates.collect({case Some(predicate) => predicate}) match {
    case definedPredicates if definedPredicates.size > 0 => or(definedPredicates)
    case _ => _ => true
  }
}

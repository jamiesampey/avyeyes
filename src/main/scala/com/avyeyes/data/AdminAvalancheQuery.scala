package com.avyeyes.data

import com.avyeyes.data.AvalanchePredicates._

case class AdminAvalancheQuery(
  extId: Option[String] = None,
  areaName: Option[String] = None,
  submitterEmail: Option[String] = None,
  order: List[(OrderField.Value, OrderDirection.Value)] = List((OrderField.CreateTime, OrderDirection.desc)),
  offset: Int = 0,
  limit: Int = Int.MaxValue)
  extends OrderedAvalancheQuery {

  def toPredicate = or(
    extIdPredicate(extId),
    areaNamePredicate(areaName),
    emailPredicate(submitterEmail)
  )
}

package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.data.AvalanchePredicates._
import com.jamiesampey.avyeyes.data.OrderDirection.OrderDirection
import com.jamiesampey.avyeyes.data.OrderField.OrderField
import com.jamiesampey.avyeyes.model.Avalanche

private[data] trait OrderedAvalancheQuery {
  def orderBy: OrderField
  def order: OrderDirection
  def offset: Int
  def limit: Int

  def toPredicate: AvalanchePredicate
  def and(predicates: List[AvalanchePredicate])(a: Avalanche) = predicates.forall(predicate => predicate(a))
  def or(predicates: List[AvalanchePredicate])(a: Avalanche) = predicates.exists(predicate => predicate(a))

  import com.jamiesampey.avyeyes.data.OrderField._
  import com.jamiesampey.avyeyes.data.OrderDirection._

  def sortFunction(x: Avalanche, y: Avalanche): Boolean = {
    val compareVal = orderBy match {
      case CreateTime => y.createTime compareTo x.createTime
      case UpdateTime => y.updateTime compareTo x.updateTime
      case ExtId => y.extId compareTo x.extId
      case Viewable => y.viewable compareTo x.viewable
      case AreaName => y.areaName.toLowerCase compareTo x.areaName.toLowerCase
      case Date => y.date compareTo x.date
      case SubmitterEmail => y.submitterEmail.toLowerCase compareTo x.submitterEmail.toLowerCase
    }

    if (order == asc) compareVal > 0 else compareVal < 0
  }
}

object OrderField extends Enumeration {
  type OrderField = Value
  val CreateTime, UpdateTime, ExtId, Viewable, AreaName, Date, SubmitterEmail = Value
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val asc, desc = Value
}

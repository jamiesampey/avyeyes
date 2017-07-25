package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.data.AvalanchePredicates._
import com.jamiesampey.avyeyes.data.OrderDirection.OrderDirection
import com.jamiesampey.avyeyes.data.OrderField.OrderField
import com.jamiesampey.avyeyes.model.Avalanche

private[data] trait OrderedAvalancheQuery {
  def order: List[(OrderField, OrderDirection)]
  def offset: Int
  def limit: Int

  def toPredicate: AvalanchePredicate
  def and(predicates: List[AvalanchePredicate])(a: Avalanche) = predicates.forall(predicate => predicate(a))
  def or(predicates: List[AvalanchePredicate])(a: Avalanche) = predicates.exists(predicate => predicate(a))

  def sortFunction(x: Avalanche, y: Avalanche): Boolean = {
    var compareVal = 0
    for (orderTuple <- order if compareVal == 0) {
      compareVal = compareField(x, y)(orderTuple)
    }

    compareVal > 0
  }

  import com.jamiesampey.avyeyes.data.OrderField._
  import com.jamiesampey.avyeyes.data.OrderDirection._

  private def compareField(x: Avalanche, y: Avalanche)(orderTuple: (OrderField, OrderDirection)) = {
    val compareValue = orderTuple._1 match {
      case CreateTime => y.createTime compareTo x.createTime
      case UpdateTime => y.updateTime compareTo x.updateTime
      case ExtId => y.extId compareTo x.extId
      case Viewable => y.viewable compareTo x.viewable
      case AreaName => y.areaName compareTo x.areaName
      case Date => y.date compareTo x.date
      case SubmitterEmail => y.submitterEmail compareTo x.submitterEmail
    }

    if (orderTuple._2 == asc) compareValue else compareValue * -1
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

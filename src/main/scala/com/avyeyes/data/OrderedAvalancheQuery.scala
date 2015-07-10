package com.avyeyes.data

import com.avyeyes.data.OrderDirection.OrderDirection
import com.avyeyes.data.OrderField.OrderField
import com.avyeyes.model.Avalanche

private[data] trait OrderedAvalancheQuery {
  def order: List[(OrderField, OrderDirection)]
  def offset: Int
  def limit: Int

  def toPredicate: Avalanche => Boolean

  def and(predicates: (Avalanche => Boolean)*)(a: Avalanche) = predicates.forall(predicate => predicate(a))
  def or(predicates: (Avalanche => Boolean)*)(a: Avalanche) = predicates.exists(predicate => predicate(a))

  def sortFunction(x: Avalanche, y: Avalanche): Boolean = {
    var compareVal = 0
    for (orderTuple <- order if compareVal == 0) {
      compareVal = compareField(x, y)(orderTuple)
    }

    compareVal > 0
  }

  import com.avyeyes.data.OrderField._
  import com.avyeyes.data.OrderDirection._

  private def compareField(x: Avalanche, y: Avalanche)(orderTuple: (OrderField, OrderDirection)) = {
    val compareValue = orderTuple._1 match {
      case CreateTime => y.createTime compareTo x.createTime
      case UpdateTime => y.updateTime compareTo x.updateTime
      case ExtId => y.extId compareTo x.extId
      case Viewable => y.viewable compareTo x.viewable
      case AreaName => y.areaName compareTo x.areaName
      case Date => y.date compareTo x.date
      case SubmitterEmail => x.submitterEmail compareTo x.submitterEmail
    }

    if (orderTuple._2 == Asc) compareValue else compareValue * -1
  }
}

object OrderField extends Enumeration {
  type OrderField = Value
  val CreateTime, UpdateTime, ExtId, Viewable, AreaName, Date, SubmitterEmail = Value
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val Asc, Desc = Value
}

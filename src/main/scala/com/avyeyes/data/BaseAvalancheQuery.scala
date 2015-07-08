package com.avyeyes.data

import com.avyeyes.data.OrderDirection.OrderDirection
import com.avyeyes.data.OrderField.OrderField
import com.avyeyes.model.Avalanche

private[data] abstract class BaseAvalancheQuery(
  orderBy: List[(OrderField, OrderDirection)],
  offset: Int,
  limit: Int) {

  def toPredicate: Avalanche => Boolean

  def and(predicates: (Avalanche => Boolean)*)(a: Avalanche) = predicates.forall(predicate => predicate(a))
  def or(predicates: (Avalanche => Boolean)*)(a: Avalanche) = predicates.exists(predicate => predicate(a))

//  def sortAndPaginate(list: List[Avalanche]): List[Avalanche] = {
//    val sortedList = orderBy(0)._2 match {
//      case asc => list.sortBy(getSortField)
//      case desc => list.sortBy(getSortField).reverse
//    }

//    sortedList.drop(offset).take(limit)
//  }

//  private def getSortField(a: Avalanche) = {
//    orderBy(0)._1 match {
//      case OrderField.createTime => a.createTime
//      case OrderField.updateTime => a.updateTime
//      case OrderField.extId => a.extId
//      case OrderField.viewable => a.viewable
//      case OrderField.areaName => a.areaName
//      case OrderField.date => a.date
//      case OrderField.submitterEmail => a.submitterEmail
//    }
//  }
}

object OrderField extends Enumeration {
  type OrderField = Value
  val createTime, updateTime, extId, viewable, areaName, date, submitterEmail = Value
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val asc, desc = Value
}

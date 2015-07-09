package com.avyeyes.data

import com.avyeyes.data.OrderDirection.OrderDirection
import com.avyeyes.data.OrderField.OrderField
import com.avyeyes.model.Avalanche

private[data] trait OrderedAvalancheQuery {
  def orderBy: List[(OrderField, OrderDirection)]
  def offset: Int
  def limit: Int

  def toPredicate: Avalanche => Boolean

  def and(predicates: (Avalanche => Boolean)*)(a: Avalanche) = predicates.forall(predicate => predicate(a))
  def or(predicates: (Avalanche => Boolean)*)(a: Avalanche) = predicates.exists(predicate => predicate(a))

  def sortAndPaginate(list: List[Avalanche]): List[Avalanche] = {
    val sortedList = orderBy.size match {
      case 1 => list.sortBy(a => getOrdering(a, orderBy(0)))
      case 2 => list.sortBy(a => (getOrdering(a, orderBy(0)), getOrdering(a, orderBy(1))) )
      case 3 => list.sortBy(a => (getOrdering(a, orderBy(0)), getOrdering(a, orderBy(1)), getOrdering(a, orderBy(2))) )
      case _ => list
    }

    sortedList.drop(offset).take(limit)
  }

  implicit def ReverseOrdering[T: Ordering]: Ordering[Reverse[T]] = Ordering[T].reverse.on(_.t)

  private def getOrdering(a: Avalanche, orderTuple: (OrderField, OrderDirection)) = {
    val orderField = orderTuple._1 match {
      case OrderField.createTime => a.createTime
      case OrderField.updateTime => a.updateTime
      case OrderField.extId => a.extId
      case OrderField.viewable => a.viewable
      case OrderField.areaName => a.areaName
      case OrderField.date => a.date
      case OrderField.submitterEmail => a.submitterEmail
    }

    orderTuple._2 match {
      case OrderDirection.asc => orderField
      case OrderDirection.desc => Reverse(orderField)
    }
  }
}

object OrderField extends Enumeration {
  type OrderField = Value
  val createTime, updateTime, extId, viewable, areaName, date, submitterEmail = Value
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val asc, desc = Value
}

case class Reverse[T](t: T)
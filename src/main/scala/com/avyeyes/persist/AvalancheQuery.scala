package com.avyeyes.persist

import com.avyeyes.util.AEHelpers._
import scala.reflect.runtime.universe._
import com.avyeyes.model.Avalanche

object AvalancheQuery {
  val DefaultOrderByField = "id"  

  lazy val AvalancheFields = typeOf[Avalanche].members.collect { 
    case m: MethodSymbol if (m.isAccessor) => m.name.toString}.toList
    
  def isAvalancheField(field: String) = {
    val found = AvalancheFields.contains(field)
    found
  }
}

case class AvalancheQuery(viewable: Option[Boolean], geo: Option[GeoBounds],
  fromDateStr: String, toDateStr: String, avyTypeStr: String, avyTriggerStr: String, 
  rSize: String, dSize: String, numCaught: String, numKilled: String,
  orderBy: String = AvalancheQuery.DefaultOrderByField, orderDirection: OrderDirection.Value = OrderDirection.ASC,
  page: Int = 0, pageLimit: Int = Int.MaxValue) {
  
  if (!isValidOrderByField(orderBy)) throw new RuntimeException(s"AvalancheQuery cannot be ordered by field '$orderBy'")
    
  private def isValidOrderByField(field: String): Boolean = field match {
     case AvalancheQuery.DefaultOrderByField => true
     case f if (AvalancheQuery.isAvalancheField(f)) => true
     case _ => false
  }
}
 
case class GeoBounds(northStr: String, eastStr: String, southStr: String, westStr: String) {
  def northLimit = strToDblOrZero(northStr)
  def eastLimit = strToDblOrZero(eastStr)
  def southLimit = strToDblOrZero(southStr)
  def westLimit = strToDblOrZero(westStr)
}

object OrderDirection extends Enumeration {
  type OrderDirection = Value
  val ASC, DESC = Value
}
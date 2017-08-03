package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.model.Avalanche
import com.jamiesampey.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.jamiesampey.avyeyes.model.enums.AvalancheType.AvalancheType
import org.joda.time.DateTime

private[data] object AvalanchePredicates {

  type AvalanchePredicate = Avalanche => Boolean

  def viewablePredicate(valueOption: Option[Boolean]): Option[AvalanchePredicate] = valueOption match {
    case Some(viewable) => Some(_.viewable == viewable)
    case None => None
  }

  def geoBoundsPredicate(valueOption: Option[GeoBounds]): Option[AvalanchePredicate] = valueOption match {
    case Some(geoBounds) =>  Some(a =>
      a.location.longitude >= geoBounds.lngMin &&
      a.location.longitude <= geoBounds.lngMax &&
      a.location.latitude >= geoBounds.latMin &&
      a.location.latitude <= geoBounds.latMax)
    case None => None
  }

  def dateRangePredicate(rangeOption: Option[(DateTime, DateTime)]): Option[AvalanchePredicate] = rangeOption match {
    case Some(dateRange) => Some(a => a.date.getMillis >= dateRange._1.getMillis && a.date.getMillis <= dateRange._2.getMillis)
    case None => None
  }

  def aspectRangePredicate(rangeOption: Option[(Int, Int)]): Option[AvalanchePredicate] = rangeOption match {
    case Some(aspectRange) => Some(a => a.slope.aspect >= aspectRange._1 && a.slope.aspect <= aspectRange._2)
    case None => None
  }

  def angleRangePredicate(rangeOption: Option[(Int, Int)]): Option[AvalanchePredicate] = rangeOption match {
    case Some(angleRange) => Some(a => (a.slope.angle >= angleRange._1) && (a.slope.angle <= angleRange._2))
    case None => None
  }

  def elevationRangePredicate(rangeOption: Option[(Int, Int)]): Option[AvalanchePredicate] = rangeOption match {
    case Some(altitudeRange) => Some(a => a.location.altitude >= altitudeRange._1 && a.location.altitude <= altitudeRange._2)
    case None => None
  }

  def avyTypePredicate(valueOption: Option[AvalancheType]): Option[AvalanchePredicate] = valueOption match {
    case Some(avyType) => Some(_.classification.avyType == avyType)
    case None => None
  }

  def triggerPredicate(valueOption: Option[AvalancheTrigger]): Option[AvalanchePredicate] = valueOption match {
    case Some(trigger) => Some(_.classification.trigger == trigger)
    case None => None
  }

  def rSizePredicate(valueOption: Option[Double]): Option[AvalanchePredicate] = valueOption match {
    case Some(rSize) => Some(_.classification.rSize >= rSize)
    case None => None
  }

  def dSizePredicate(valueOption: Option[Double]): Option[AvalanchePredicate] = valueOption match {
    case Some(dSize) => Some(_.classification.dSize >= dSize)
    case None => None
  }

  def caughtPredicate(valueOption: Option[Int]): Option[AvalanchePredicate] = valueOption match {
    case Some(caught) => Some(_.humanNumbers.caught >= caught)
    case None => None
  }

  def killedPredicate(valueOption: Option[Int]): Option[AvalanchePredicate] = valueOption match {
    case Some(killed) => Some(_.humanNumbers.killed >= killed)
    case None => None
  }

  def extIdPredicate(valueOption: Option[String]): Option[AvalanchePredicate] = valueOption match {
    case Some(extId) => Some(_.extId.toLowerCase contains extId.toLowerCase)
    case None => None
  }

  def areaNamePredicate(valueOption: Option[String]): Option[AvalanchePredicate] = valueOption match {
    case Some(areaName) => Some(_.areaName.toLowerCase contains areaName.toLowerCase)
    case None => None
  }

  def emailPredicate(valueOption: Option[String]): Option[AvalanchePredicate] = valueOption match {
    case Some(email) => Some(_.submitterEmail.toLowerCase contains email.toLowerCase)
    case None => None
  }
}

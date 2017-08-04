package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.model.Avalanche
import com.jamiesampey.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
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

  def fromDatePredicate(valueOption: Option[DateTime]): Option[AvalanchePredicate] = valueOption match {
    case Some(fromDate) => Some(_.date.getMillis >= fromDate.getMillis)
    case None => None
  }

  def toDatePredicate(valueOption: Option[DateTime]): Option[AvalanchePredicate] = valueOption match {
    case Some(toDate) => Some(_.date.getMillis <= toDate.getMillis)
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

  def interfacePredicate(valueOption: Option[AvalancheInterface]): Option[AvalanchePredicate] = valueOption match {
    case Some(interface) => Some(_.classification.interface == interface)
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

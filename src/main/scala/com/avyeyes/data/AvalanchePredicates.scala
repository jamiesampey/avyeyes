package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import org.joda.time.DateTime

private[data] object AvalanchePredicates {

  def viewablePredicate(valueOption: Option[Boolean])(a: Avalanche) = valueOption match {
    case Some(viewable) => a.viewable == viewable
    case None => true
  }

  def geoBoundsPredicate(valueOption: Option[GeoBounds])(a: Avalanche) = valueOption match {
    case Some(geoBounds) => (
      a.location.longitude >= geoBounds.lngMin
      && a.location.longitude <= geoBounds.lngMax
      && a.location.latitude >= geoBounds.latMin
      && a.location.latitude <= geoBounds.latMax)
    case None => true
  }

  def fromDatePredicate(valueOption: Option[DateTime])(a: Avalanche) = valueOption match {
    case Some(fromDate) => a.date.getMillis >= fromDate.getMillis
    case None => true
  }

  def toDatePredicate(valueOption: Option[DateTime])(a: Avalanche) = valueOption match {
    case Some(toDate) => a.date.getMillis <= toDate.getMillis
    case None => true
  }

  def avyTypePredicate(valueOption: Option[AvalancheType])(a: Avalanche) = valueOption match {
    case Some(avyType) => a.classification.avyType == avyType
    case None => true
  }

  def triggerPredicate(valueOption: Option[AvalancheTrigger])(a: Avalanche) = valueOption match {
    case Some(trigger) => a.classification.trigger == trigger
    case None => true
  }

  def rSizePredicate(valueOption: Option[Double])(a: Avalanche) = valueOption match {
    case Some(rSize) => a.classification.rSize >= rSize
    case None => true
  }

  def dSizePredicate(valueOption: Option[Double])(a: Avalanche) = valueOption match {
    case Some(dSize) => a.classification.dSize >= dSize
    case None => true
  }

  def caughtPredicate(valueOption: Option[Int])(a: Avalanche) = valueOption match {
    case Some(caught) => a.humanNumbers.caught >= caught
    case None => true
  }

  def killedPredicate(valueOption: Option[Int])(a: Avalanche) = valueOption match {
    case Some(killed) => a.humanNumbers.killed >= killed
    case None => true
  }

  def extIdPredicate(valueOption: Option[String])(a: Avalanche) = valueOption match {
    case Some(extId) => a.extId.toLowerCase contains extId.toLowerCase
    case None => true
  }

  def areaNamePredicate(valueOption: Option[String])(a: Avalanche) = valueOption match {
    case Some(areaName) => a.areaName.toLowerCase contains areaName.toLowerCase
    case None => true
  }

  def emailPredicate(valueOption: Option[String])(a: Avalanche) = valueOption match {
    case Some(email) => a.submitterEmail.toLowerCase contains email.toLowerCase
    case None => true
  }
}

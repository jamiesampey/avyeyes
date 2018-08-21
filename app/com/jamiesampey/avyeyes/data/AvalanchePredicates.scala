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

  def avyTypesPredicate(valueOptions: Option[Seq[AvalancheType]]): Option[AvalanchePredicate] = valueOptions match {
    case Some(avyTypes) => Some(a => avyTypes.contains(a.classification.avyType))
    case None => None
  }

  def triggersPredicate(valueOptions: Option[Seq[AvalancheTrigger]]): Option[AvalanchePredicate] = valueOptions match {
    case Some(triggers) => Some(a => triggers.contains(a.classification.trigger))
    case None => None
  }

  def interfacesPredicate(valueOptions: Option[Seq[AvalancheInterface]]): Option[AvalanchePredicate] = valueOptions match {
    case Some(interfaces) => Some(a => interfaces.contains(a.classification.interface))
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

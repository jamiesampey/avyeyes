package com.avyeyes.controllers

import javax.inject.Inject

import com.avyeyes.data.{AvalancheQuery, GeoBounds, OrderDirection, OrderField}
import com.avyeyes.model.enums.{AvalancheTrigger, AvalancheType}
import com.avyeyes.util.Converters.strToDate
import com.avyeyes.util.Constants.CamAltitudeLimit
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}


object AvalancheQueryBinder {

  implicit object AvalancheQueryBindable extends QueryStringBindable[AvalancheQuery] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, AvalancheQuery]] = for {
      lngMax <- params.get("lngMax").map(_.head.toDouble)
      lngMin <- params.get("lngMin").map(_.head.toDouble)
      latMax <- params.get("latMax").map(_.head.toDouble)
      latMin <- params.get("latMin").map(_.head.toDouble)
      camAlt <- params.get("camAlt").map(_.head.toDouble)
    } yield Try {

      //      if (camAlt > CamAltitudeLimit) {
      //        throw new RuntimeException(Messages("msg.eyeTooHigh"))
      //      }

      AvalancheQuery(
        viewable = Some(true),
        geoBounds = Some(GeoBounds(lngMax, lngMin, latMax, latMin)),
        fromDate = firstNonEmptyValue(params.get("fromDate")).map(strToDate),
        toDate = firstNonEmptyValue(params.get("toDate")).map(strToDate),
        avyType = firstNonEmptyValue(params.get("avyType")).map(AvalancheType.fromCode),
        trigger = firstNonEmptyValue(params.get("trigger")).map(AvalancheTrigger.fromCode),
        rSize = firstNonEmptyValue(params.get("rSize")).map(_.toDouble),
        dSize = firstNonEmptyValue(params.get("dSize")).map(_.toDouble),
        numCaught = firstNonEmptyValue(params.get("numCaught")).map(_.toInt),
        numKilled = firstNonEmptyValue(params.get("numKilled")).map(_.toInt),
        order = List((OrderField.Date, OrderDirection.desc))
      )
    } match {
      case Success(avalancheQuery) => Right(avalancheQuery)
      case Failure(ex) => Left(ex.getMessage)
    }

    private def firstNonEmptyValue(possibleValues: Option[Seq[String]]): Option[String] = possibleValues.flatMap(_.find(_.nonEmpty))

    override def unbind(key: String, query: AvalancheQuery) = "unimplemented"
  }
}

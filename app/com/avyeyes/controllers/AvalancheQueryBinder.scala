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
      camAlt <- params.get("camAlt").map(_.head.toInt)
    } yield Try {

//      if (camAlt > CamAltitudeLimit) {
//        throw new RuntimeException(Messages("msg.eyeTooHigh"))
//      }

      AvalancheQuery(
        viewable = Some(true),
        geoBounds = Some(GeoBounds(lngMax, lngMin, latMax, latMin)),
        fromDate = params.get("fromDate").map(params => strToDate(params.head)),
        toDate = params.get("toDate").map(params => strToDate(params.head)),
        avyType = params.get("avyType").map(params => AvalancheType.fromCode(params.head)),
        trigger = params.get("trigger").map(params => AvalancheTrigger.fromCode(params.head)),
        rSize = params.get("rSize").map(_.head.toDouble),
        dSize = params.get("dSize").map(_.head.toDouble),
        numCaught = params.get("numCaught").map(_.head.toInt),
        numKilled = params.get("numKilled").map(_.head.toInt),
        order = List((OrderField.Date, OrderDirection.desc))
      )
    } match {
      case Success(avalancheQuery) => Right(avalancheQuery)
      case Failure(ex) => Left(ex.getMessage)
    }

    override def unbind(key: String, query: AvalancheQuery) = "unimplemented"
  }
}

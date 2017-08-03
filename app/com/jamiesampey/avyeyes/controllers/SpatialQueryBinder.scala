package com.jamiesampey.avyeyes.controllers

import com.jamiesampey.avyeyes.data.{AvalancheSpatialQuery, GeoBounds, OrderDirection, OrderField}
import com.jamiesampey.avyeyes.model.enums.{AvalancheTrigger, AvalancheType}
import com.jamiesampey.avyeyes.util.Converters.strToDate
import org.joda.time.DateTime
import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}


object SpatialQueryBinder {

  implicit def SpatialQueryBindable(implicit doubleBinder: QueryStringBindable[Double]) = new QueryStringBindable[AvalancheSpatialQuery] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, AvalancheSpatialQuery]] = for {
      lngMaxParam <- doubleBinder.bind("lngMax", params)
      lngMinParam <- doubleBinder.bind("lngMin", params)
      latMaxParam <- doubleBinder.bind("latMax", params)
      latMinParam <- doubleBinder.bind("latMin", params)
    } yield Try {

      val geoBoundsOpt = (lngMaxParam, lngMinParam, latMaxParam, latMinParam) match {
        case (Right(lngMax), Right(lngMin), Right(latMax), Right(latMin)) => Some(GeoBounds(lngMax, lngMin, latMax, latMin))
        case _ => None
      }

      val fromDate = firstNonEmptyValue(params.get("fromDate")).map(strToDate).getOrElse(new DateTime(0))
      val toDate = firstNonEmptyValue(params.get("toDate")).map(strToDate).getOrElse(DateTime.now)

      val aspectRange = toOptionalIntRange(firstNonEmptyValue(params.get("aspectLow")), firstNonEmptyValue(params.get("aspectHigh")))
      val angleRange = toOptionalIntRange(firstNonEmptyValue(params.get("angleLow")), firstNonEmptyValue(params.get("angleHigh")))
      val elevationRange = toOptionalIntRange(firstNonEmptyValue(params.get("elevationLow")), firstNonEmptyValue(params.get("elevationHigh")))

      AvalancheSpatialQuery(
        viewable = Some(true),
        geoBounds = geoBoundsOpt,
        dateRange = Some((fromDate, toDate)),
        aspectRange = aspectRange,
        angleRange = angleRange,
        elevationRange = elevationRange,
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

    private def toOptionalIntRange(lowStrValue: Option[String], highStrValue: Option[String]): Option[(Int, Int)] = {
      (lowStrValue.map(_.toInt), highStrValue.map(_.toInt)) match {
        case (Some(low), Some(high)) => Some((low, high))
        case _ => None
      }
    }

    override def unbind(key: String, query: AvalancheSpatialQuery) = "unimplemented"
  }
}

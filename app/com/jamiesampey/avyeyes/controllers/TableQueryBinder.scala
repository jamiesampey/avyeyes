package com.jamiesampey.avyeyes.controllers

import com.jamiesampey.avyeyes.data.{AvalancheTableQuery, OrderDirection, OrderField}
import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}

object TableQueryBinder {

  implicit object TableQueryBindable extends QueryStringBindable[AvalancheTableQuery] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, AvalancheTableQuery]] = for {
      offset <- params.get("start").map(_.head.toInt)
      limit <- params.get("length").map(_.head.toInt)
      orderBy <- params.get("orderBy").map(_.head)
      order <- params.get("order").map(_.head)
    } yield Try {
      val searchTerm = params.get("filter").map(_.head)

      AvalancheTableQuery(
        extId = searchTerm,
        areaName = searchTerm,
        submitterEmail = searchTerm,
        orderBy = OrderField.withName(orderBy),
        order = OrderDirection.withName(order),
        offset = offset,
        limit = limit
      )
    } match {
      case Success(adminQuery) => Right(adminQuery)
      case Failure(ex) => Left(ex.getMessage)
    }

    override def unbind(key: String, adminQuery: AvalancheTableQuery) = "unimplemented"
  }
}

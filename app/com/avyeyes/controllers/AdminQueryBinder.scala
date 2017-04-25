package com.avyeyes.controllers

import com.avyeyes.data.{AdminAvalancheQuery, OrderDirection, OrderField}
import play.api.mvc.QueryStringBindable

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object AdminQueryBinder {

  implicit object AdminQueryBindable extends QueryStringBindable[AdminAvalancheQuery] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, AdminAvalancheQuery]] = for {
      offsetVal <- params.get("start").map(_.head.toInt)
      limitVal <- params.get("length").map(_.head.toInt)
    } yield Try {
      val orderByList = {
        val listBuffer: ListBuffer[(OrderField.Value, OrderDirection.Value)] = new ListBuffer()
        val orderColumnEntries = params.filterKeys(key => orderFieldRegex.findFirstMatchIn(key).isDefined).toList.sortBy(_._1)

        orderColumnEntries.foreach { entryTuple =>
          val orderIdx = orderFieldGroupCaptureRegex.findFirstMatchIn(entryTuple._1).map(_.group(1)).getOrElse(
            throw new RuntimeException("Table data error. Could not extract order index")
          )

          val columnIdx = entryTuple._2.head

          params.get(s"columns[$columnIdx][name]").foreach { orderColumnNameList =>
            params.get(s"order[$orderIdx][dir]").foreach { orderDirectionList =>
              listBuffer.append((OrderField.withName(orderColumnNameList.head), OrderDirection.withName(orderDirectionList.head)))
            }
          }
        }

        listBuffer.toList
      }

      val searchTerm: Option[String] = params.collectFirst {
        case entryTuple if searchFieldRegex.findFirstMatchIn(entryTuple._1).isDefined && entryTuple._2.head.nonEmpty => s"${entryTuple._2.head}"
      }

      AdminAvalancheQuery(
        extId = searchTerm,
        areaName = searchTerm,
        submitterEmail = searchTerm,
        order = orderByList,
        offset = offsetVal,
        limit = limitVal
      )
    } match {
      case Success(adminQuery) => Right(adminQuery)
      case Failure(ex) => Left(ex.getMessage)
    }

    override def unbind(key: String, adminQuery: AdminAvalancheQuery) = "unimplemented"

    private val orderFieldRegex = "order\\[\\d+\\]\\[column\\]".r
    private val orderFieldGroupCaptureRegex ="order\\[(\\d+)\\]\\[column\\]".r
    private val searchFieldRegex = "search\\[value\\]".r
  }
}

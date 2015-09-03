package com.avyeyes.rest

import com.avyeyes.data.{AdminAvalancheQuery, OrderDirection, OrderField}
import com.avyeyes.model._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Helpers._
import net.liftweb.common.{Full, Loggable}
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{InternalServerErrorResponse, JsonResponse, Req, UnauthorizedResponse}
import net.liftweb.json.JsonAST._
import org.joda.time.format.DateTimeFormat

import scala.collection.mutable.ListBuffer

class AdminTable extends RestHelper with Loggable {
  lazy val dal = Injectors.dal.vend
  lazy val userSession = Injectors.user.vend

  private val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  serve {
    case "rest" :: "admintable" :: Nil JsonGet req => {
      userSession.isAuthorizedSession() match {
        case false => UnauthorizedResponse("AvyEyes auth required")
        case true => buildResponse(req)
      }
    }
  }

  private def buildResponse(req: Req) = {
    try {
      val queryResult = dal.getAvalanchesAdmin(buildQuery(req))
      JsonResponse(toDataTablesJson(queryResult, req))
    } catch {
      case e: Exception => InternalServerErrorResponse()
    }
  }

  private def buildQuery(req: Req): AdminAvalancheQuery = {
    val offsetVal = req.param("start") match {
      case Full(str) => str.toInt
      case _ => {
        logger.error("Table data 'start' param missing")
        throw new IllegalArgumentException
      }
    }

    val limitVal = req.param("length") match {
      case Full(str) => str.toInt
      case _ => {
        logger.error("Table data 'length' param missing")
        throw new IllegalArgumentException
      }
    }

    val orderByList = {
      val listBuffer: ListBuffer[(OrderField.Value, OrderDirection.Value)] = new ListBuffer()

      val orderColumnEntries = req.params filterKeys(key => "order\\[\\d+\\]\\[column\\]".r.findFirstMatchIn(key) isDefined)

      orderColumnEntries.toList sortBy (_._1) foreach (entryTuple => {
        val orderIdx = "order\\[(\\d+)\\]\\[column\\]".r.findFirstMatchIn(entryTuple._1) match {
          case Some(m) => m.group(1)
          case None => {
            logger.error("Table data error. Could not extract order index")
            throw new Exception
          }
        }

        val columnIdx = entryTuple._2(0)

        req.params.get(s"columns[$columnIdx][name]") match {
          case Some(orderColumnNameList) => {
            req.params.get(s"order[$orderIdx][dir]") match {
              case Some(orderDirectionList) => {
                listBuffer append((OrderField.withName(orderColumnNameList(0)),
                  OrderDirection.withName(orderDirectionList(0))))
              }
              case None => {
                logger.error("Table data error. Order direction param missing")
                throw new Exception
              }
            }
          }
          case None => {
            logger.error("Table data error. Order column name param missing")
            throw new Exception
          }
        }
      })

      listBuffer.toList
    }

    val searchTerm = (req.params find(entryTuple =>
      "search\\[value\\]".r.findFirstMatchIn(entryTuple._1) isDefined)) match {
      case Some(entryTuple) if !entryTuple._2(0).isEmpty => Some(s"${entryTuple._2(0)}")
      case _ => None
    }

    AdminAvalancheQuery(
      extId = searchTerm,
      areaName = searchTerm,
      submitterEmail = searchTerm,
      order = orderByList,
      offset = offsetVal,
      limit = limitVal
    )
  }

  private def toDataTablesJson(queryResult: (List[Avalanche], Int, Int), req: Req): JObject = {
    val drawVal = req.param("draw") match {
      case Full(str) => str.toInt
      case _ => {
        logger.error("Table data 'draw' param missing")
        throw new IllegalArgumentException
      }
    }

    val matchingAvalanches = queryResult._1
    val filteredRecordCount = queryResult._2
    val totalRecordCount = queryResult._3

    JObject(List(
      JField("draw", JInt(drawVal)),
      JField("recordsTotal", JInt(totalRecordCount)),
      JField("recordsFiltered", JInt(filteredRecordCount)),
      JField("data", JArray(
        matchingAvalanches map (a => JArray(List(
            JString(a.createTime.toString(dtf)),
            JString(a.updateTime.toString(dtf)),
            JString(a.extId),
            JString(getViewableElem(a.viewable)),
            JString(getHttpsAvalancheLink(a)),
            JString(a.submitterEmail))))
      ))
    ))
  }

  private def getHttpsAvalancheLink(a: Avalanche) = {
    <a href={getHttpsBaseUrl + a.extId} target="adminViewWindow">{s"${a.getTitle()}"}</a>.toString
  }

  private def getViewableElem(viewable: Boolean) = viewable match {
    case true => <span style="color: green;">Yes</span>.toString
    case false => <span style="color: red;">No</span>.toString
  }
}
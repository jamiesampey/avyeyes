package com.avyeyes.rest

import java.text.SimpleDateFormat

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Helpers._
import com.avyeyes.persist.AvyEyesSqueryl._
import com.avyeyes.persist._
import com.avyeyes.snippet.AdminConsole._
import net.liftweb.common.Loggable
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{JsonResponse, UnauthorizedResponse}
import net.liftweb.json.JsonAST._


object AdminTable extends RestHelper with Loggable {
  lazy val avyDao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  serve {
    case "rest" :: "admintable" :: Nil JsonGet req => {
      transaction {
        isAuthorizedSession match {
          case false => UnauthorizedResponse("Avy Eyes auth required")
          case true => {
            val avalanches = avyDao.selectAvalanches(AvalancheQuery.baseQuery.copy(
              viewable = None, orderBy = OrderBy.CreateTime, order = Order.Desc))
            JsonResponse(toDataTablesJson(avalanches))
          }
        }
      }
    }
  }

  private def toDataTablesJson(avalanches: List[Avalanche]): JObject = {
    JObject(List(
      JField("draw", JInt(1)),
      JField("recordsTotal", JInt(avalanches.size)),
      JField("recordsFiltered", JInt(avalanches.size)),
      JField("data", JArray(
        avalanches map (a => JArray(List(
          JString(sdf.format(a.createTime)),
          JString(sdf.format(a.updateTime)),
          JString(a.extId),
          JString(getViewableElem(a.viewable)),
          JString(getHttpsAvalancheLink(a)),
          JString(a.submitter.single.email)))
      )))
    ))
  }

  private def getHttpsAvalancheLink(a: Avalanche): String = {
    <a href={getHttpsBaseUrl + a.extId} target="_blank">{s"${a.getTitle()}"}</a>.toString
  }

  private def getViewableElem(viewable: Boolean): String = {
    (if (viewable)
      <span style="color: green;">Yes</span>
    else
      <span style="color: red;">No</span>
    ).toString
  }
}
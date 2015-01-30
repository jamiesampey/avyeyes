package com.avyeyes.rest

import com.avyeyes.persist.{OrderDirection, OrderField, AdminAvalancheQuery}
import com.avyeyes.test._
import net.liftweb.http._
import org.mockito.Mockito._

class AdminTableTest extends WebSpec2 with MockInjectors with AvalancheHelpers with LiftHelpers {
  sequential
  // Testing an OBJECT (singleton), so the mockAvalancheDao is inserted ONCE.
  // Only one chance to mock all methods.

  when(mockUserSession.isAuthorizedSession).thenReturn(false).thenReturn(true)

  mockAvalancheDao.selectAvalanchesForAdminTable(any[AdminAvalancheQuery]) returns ((Nil, 0, 0))


  "Admin table error handling" should {
    "Returns UnauthorizedResponse if session is not authorized" withSFor("http://avyeyes.com/rest/admintable") in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(AdminTable(req)())

      resp must beAnInstanceOf[UnauthorizedResponse]
    }

    "Returns InternalServerErrorResponse if an error occurs" withSFor("http://avyeyes.com/rest/admintable") in {
      val req = openLiftReqBox(S.request) // req does not contain datatable query params
      val resp = openLiftRespBox(AdminTable(req)())
      resp must beAnInstanceOf[InternalServerErrorResponse]
    }
  }

  "Admin table query" should {
    "Extracts offset, limit, orderby, and search params from request" withSFor("http://avyeyes.com/rest/admintable") in {
      val searchTerm = "gmail.com"

      val dataTablesParams = Map("start" -> "10", "length" -> "20",
        "order[0][column]" -> "3", "order[1][column]" -> "0",
        "order[0][dir]" -> "desc", "order[1][dir]" -> "asc",
        "columns[0][name]" -> "createTime", "columns[3][name]" -> "viewable",
        "search[value]" -> searchTerm)

      val req = addParamsToReq(openLiftReqBox(S.request), dataTablesParams)
      AdminTable(req)()

      val queryArgCapture = capture[AdminAvalancheQuery]
      there was one(mockAvalancheDao).selectAvalanchesForAdminTable(queryArgCapture)
      val adminQuery = queryArgCapture.value

      adminQuery.offset must_== 10
      adminQuery.limit must_== 20
      adminQuery.orderBy(0) must_== (OrderField.viewable, OrderDirection.desc)
      adminQuery.orderBy(1) must_== (OrderField.createTime, OrderDirection.asc)
      adminQuery.extId must_== Some(s"%$searchTerm%")
      adminQuery.areaName must_== Some(s"%$searchTerm%")
      adminQuery.submitterEmail must_== Some(s"%$searchTerm%")
    }
  }
}

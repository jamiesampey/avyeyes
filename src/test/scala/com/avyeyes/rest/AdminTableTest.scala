package com.avyeyes.rest

import com.avyeyes.data.{OrderDirection, OrderField, AdminAvalancheQuery}
import com.avyeyes.model.OrderDirection
import com.avyeyes.test._
import net.liftweb.http._

class AdminTableTest extends WebSpec2 with MockInjectors with Generators with LiftHelpers {
  sequential

  val adminTable = new AdminTable
  val adminTableUrl = "https://avyeyes.com/rest/admintable"

  "Admin table error handling" should {
    "Returns UnauthorizedResponse if session is not authorized" withSFor(adminTableUrl) in {
      mockUserSession.isAuthorizedSession returns false
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(adminTable(req)())
      resp must beAnInstanceOf[UnauthorizedResponse]
    }

    "Returns InternalServerErrorResponse if an error occurs" withSFor(adminTableUrl) in {
      mockUserSession.isAuthorizedSession returns true
      val req = openLiftReqBox(S.request) // req does not contain datatable query params
      val resp = openLiftRespBox(adminTable(req)())
      resp must beAnInstanceOf[InternalServerErrorResponse]
    }
  }

  "Admin table query" should {
    mockUserSession.isAuthorizedSession returns true

    val a1 = genAvalanche.sample.get.copy(extId = "950503kf", areaName = "West side of berthoud", submitterEmail = "jeffery.lebowski@yahoo.com")
    val a2 = genAvalanche.sample.get.copy(extId = "jh984f9d", areaName = "New York mtn", submitterEmail = "walter_sobchak@gmail.com")
    val a3 = genAvalanche.sample.get.copy(extId = "g4ifj390", areaName = "Vail Pass, black lakes ridge", submitterEmail = "donny@hiselement.com")

    val totalRecordsInDb = 84923
    val filteredRecords = 3
    mockAvalancheDao.getAvalanchesAdmin(any[AdminAvalancheQuery]) returns ((a1::a2::a3::Nil, filteredRecords, totalRecordsInDb))

    var drawParam = "43"
    val searchParam = "gmail.com"
    val dataTablesParams = Map("draw" -> drawParam, "start" -> "10", "length" -> "20",
      "order[0][column]" -> "3", "order[1][column]" -> "0",
      "order[0][dir]" -> "desc", "order[1][dir]" -> "asc",
      "columns[0][name]" -> "createTime", "columns[3][name]" -> "viewable",
      "search[value]" -> searchParam)

    "Extract offset, limit, orderby, and search params from request" withSFor (adminTableUrl) in {
      val req = addParamsToReq(openLiftReqBox(S.request), dataTablesParams)
      adminTable(req)()

      val queryArgCapture = capture[AdminAvalancheQuery]
      there was one(mockAvalancheDao).getAvalanchesAdmin(queryArgCapture)
      val adminQuery = queryArgCapture.value

      adminQuery.offset must_== 10
      adminQuery.limit must_== 20
      adminQuery.order(0) must_==(OrderField.Viewable, OrderDirection.desc)
      adminQuery.order(1) must_==(OrderField.CreateTime, OrderDirection.asc)
      adminQuery.extId must_== Some(s"%$searchParam%")
      adminQuery.areaName must_== Some(s"%$searchParam%")
      adminQuery.submitterEmail must_== Some(s"%$searchParam%")
    }

    "Construct a JSON response" withSFor (adminTableUrl) in {
      val req = addParamsToReq(openLiftReqBox(S.request), dataTablesParams)
      val resp = openLiftRespBox(adminTable(req)())

      val generatedJson = resp.asInstanceOf[JsonResponse].json.toJsCmd

      extractJsonIntField(resp, "draw") must_== drawParam.toInt
      extractJsonIntField(resp, "recordsTotal") must_== totalRecordsInDb
      extractJsonIntField(resp, "recordsFiltered") must_== filteredRecords
      generatedJson must contain(a1.extId)
      generatedJson must contain(a2.extId)
      generatedJson must contain(a3.extId)
      generatedJson must contain(a1.areaName)
      generatedJson must contain(a2.areaName)
      generatedJson must contain(a3.areaName)
      generatedJson must contain(a1.submitterEmail)
      generatedJson must contain(a2.submitterEmail)
      generatedJson must contain(a3.submitterEmail)
    }
  }
}

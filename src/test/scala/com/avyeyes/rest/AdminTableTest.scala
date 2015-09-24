package com.avyeyes.rest

import com.avyeyes.data.{CachedDAL, AdminAvalancheQuery, OrderDirection, OrderField}
import com.avyeyes.service.{UserSession, Injectors}
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import com.avyeyes.test.LiftHelpers._
import net.liftweb.http._
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

class AdminTableTest extends WebSpec2 with AroundExample with Mockito {
  isolated

  val mockAvalancheDal = mock[CachedDAL]
  val mockUserSession = mock[UserSession]

  def around[T: AsResult](t: => T): Result =
    Injectors.user.doWith(mockUserSession) {
      Injectors.dal.doWith(mockAvalancheDal) {
        AsResult(t)
      }
    }

  val adminTableUrl = "https://avyeyes.com/rest/admintable"

  "Admin table error handling" should {
    "Returns UnauthorizedResponse if session is not authorized" withSFor(adminTableUrl) in {
      mockUserSession.isAuthorizedSession returns false
      val adminTable = new AdminTable

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(adminTable(req)())
      resp must beAnInstanceOf[UnauthorizedResponse]
    }

    "Returns InternalServerErrorResponse if an error occurs" withSFor(adminTableUrl) in {
      mockUserSession.isAuthorizedSession returns true
      val adminTable = new AdminTable

      val req = openLiftReqBox(S.request) // req does not contain datatable query params
      val resp = openLiftRespBox(adminTable(req)())
      resp must beAnInstanceOf[InternalServerErrorResponse]
    }
  }

  "Admin table query" should {
    mockUserSession.isAuthorizedSession returns true

    val a1 = avalancheForTest.copy(extId = "950503kf", areaName = "West side of berthoud", submitterEmail = "jeffery.lebowski@yahoo.com")
    val a2 = avalancheForTest.copy(extId = "jh984f9d", areaName = "New York mtn", submitterEmail = "walter_sobchak@gmail.com")
    val a3 = avalancheForTest.copy(extId = "g4ifj390", areaName = "Vail Pass, black lakes ridge", submitterEmail = "donny@hiselement.com")

    val totalRecordsInDb = 84923
    val filteredRecords = 3
    mockAvalancheDal.getAvalanchesAdmin(any[AdminAvalancheQuery]) returns ((a1::a2::a3::Nil, filteredRecords, totalRecordsInDb))

    var drawParam = "43"
    val searchParam = "gmail.com"
    val dataTablesParams = Map("draw" -> drawParam, "start" -> "10", "length" -> "20",
      "order[0][column]" -> "3", "order[1][column]" -> "0",
      "order[0][dir]" -> "desc", "order[1][dir]" -> "asc",
      "columns[0][name]" -> "CreateTime", "columns[3][name]" -> "Viewable",
      "search[value]" -> searchParam)

    "Extract offset, limit, orderby, and search params from request" withSFor (adminTableUrl) in {
      val adminTable = new AdminTable

      val req = addParamsToReq(openLiftReqBox(S.request), dataTablesParams)
      adminTable(req)()

      val queryArgCapture = capture[AdminAvalancheQuery]
      there was one(mockAvalancheDal).getAvalanchesAdmin(queryArgCapture)
      val adminQuery = queryArgCapture.value

      adminQuery.offset mustEqual 10
      adminQuery.limit mustEqual 20
      adminQuery.order(0) mustEqual(OrderField.Viewable, OrderDirection.desc)
      adminQuery.order(1) mustEqual(OrderField.CreateTime, OrderDirection.asc)
      adminQuery.extId mustEqual Some(s"$searchParam")
      adminQuery.areaName mustEqual Some(s"$searchParam")
      adminQuery.submitterEmail mustEqual Some(s"$searchParam")
    }

    "Construct a JSON response" withSFor (adminTableUrl) in {
      val adminTable = new AdminTable

      val req = addParamsToReq(openLiftReqBox(S.request), dataTablesParams)
      val resp = openLiftRespBox(adminTable(req)())

      val generatedJson = resp.asInstanceOf[JsonResponse].json.toJsCmd

      extractJsonIntField(resp, "draw") mustEqual drawParam.toInt
      extractJsonIntField(resp, "recordsTotal") mustEqual totalRecordsInDb
      extractJsonIntField(resp, "recordsFiltered") mustEqual filteredRecords
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

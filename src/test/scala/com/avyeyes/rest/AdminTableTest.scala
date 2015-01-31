package com.avyeyes.rest

import com.avyeyes.persist.{AdminAvalancheQuery, OrderDirection, OrderField}
import com.avyeyes.test._
import net.liftweb.http._
import org.mockito.Mockito._

class AdminTableTest extends WebSpec2 with MockInjectors with AvalancheHelpers with LiftHelpers {
  sequential
  // Testing an OBJECT (singleton), so the mockAvalancheDao is inserted ONCE.
  // Only one chance to mock all methods.
  val adminTableUrl = "https://avyeyes.com/rest/admintable"

  val commonLat = 38.5763463456
  val commonLng = -102.5359593
  val a1 = avalancheWithNameAndSubmitter("950503kf", true, commonLat, commonLng, "West side of berthoud", "jeffery.lebowski@yahoo.com")
  val a2 = avalancheWithNameAndSubmitter("jh984f9d", false, commonLat, commonLng, "New York mtn", "walter_sobchak@gmail.com")
  val a3 = avalancheWithNameAndSubmitter("g4ifj390", true, commonLat, commonLng, "Vail Pass, black lakes ridge", "donny@hiselement.com")
  mockAvalancheDao.selectAvalanchesForAdminTable(any[AdminAvalancheQuery]) returns ((a1::a2::a3::Nil, 3, 84923))

  when(mockUserSession.isAuthorizedSession).thenReturn(false).thenReturn(true)

  "Admin table error handling" should {
    "Returns UnauthorizedResponse if session is not authorized" withSFor(adminTableUrl) in {
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(AdminTable(req)())

      resp must beAnInstanceOf[UnauthorizedResponse]
    }

    "Returns InternalServerErrorResponse if an error occurs" withSFor(adminTableUrl) in {
      val req = openLiftReqBox(S.request) // req does not contain datatable query params
      val resp = openLiftRespBox(AdminTable(req)())
      resp must beAnInstanceOf[InternalServerErrorResponse]
    }
  }

  "Admin table query" should {
    val searchTerm = "gmail.com"
    val dataTablesParams = Map("draw" -> "43", "start" -> "10", "length" -> "20",
      "order[0][column]" -> "3", "order[1][column]" -> "0",
      "order[0][dir]" -> "desc", "order[1][dir]" -> "asc",
      "columns[0][name]" -> "createTime", "columns[3][name]" -> "viewable",
      "search[value]" -> searchTerm)

    "Extracts offset, limit, orderby, and search params from request" withSFor (adminTableUrl) in {
      val req = addParamsToReq(openLiftReqBox(S.request), dataTablesParams)
      AdminTable(req)()

      val queryArgCapture = capture[AdminAvalancheQuery]
      there was one(mockAvalancheDao).selectAvalanchesForAdminTable(queryArgCapture)
      val adminQuery = queryArgCapture.value

      adminQuery.offset must_== 10
      adminQuery.limit must_== 20
      adminQuery.orderBy(0) must_==(OrderField.viewable, OrderDirection.desc)
      adminQuery.orderBy(1) must_==(OrderField.createTime, OrderDirection.asc)
      adminQuery.extId must_== Some(s"%$searchTerm%")
      adminQuery.areaName must_== Some(s"%$searchTerm%")
      adminQuery.submitterEmail must_== Some(s"%$searchTerm%")
    }

    "Constructs a JSON response" withSFor (adminTableUrl) in {
      val req = addParamsToReq(openLiftReqBox(S.request), dataTablesParams)
      val resp = openLiftRespBox(AdminTable(req)())

      resp must beAnInstanceOf[JsonResponse]
    }
  }
}

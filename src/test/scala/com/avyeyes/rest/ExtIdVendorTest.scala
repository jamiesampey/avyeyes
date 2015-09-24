package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.Avalanche
import com.avyeyes.service.{ResourceService, Injectors}
import com.avyeyes.test._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.util.Validators.isValidExtId
import net.liftweb.http._
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

class ExtIdVendorTest extends WebSpec2 with AroundExample with Mockito {
  isolated

  val mockAvalancheDal = mock[CachedDAL]
  val mockResources = mock[ResourceService]

  def around[T: AsResult](t: => T): Result =
    Injectors.dal.doWith(mockAvalancheDal) {
      Injectors.resources.doWith(mockResources) {
        AsResult(t)
      }
    }

  val noAvalanche: Option[Avalanche] = None
  
  "Valid external ID request" should {
    "Return a new external ID" withSFor "http://avyeyes.com/rest/reserveExtId" in {
      mockAvalancheDal.getAvalanche(any[String]) returns(noAvalanche)
      mockResources.getIntProperty("extId.newIdAttemptLimit") returns 3

      val extIdVendor = new ExtIdVendor

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(extIdVendor(req)())
      val returnedExtId = extractJsonStringField(resp, "extId")
     
      resp must beAnInstanceOf[JsonResponse]
      isValidExtId(Some(returnedExtId)) must beTrue
    }
    
    "Return InternalServerErrorResponse (500) if an extId could not be reserved" withSFor "http://avyeyes.com/rest/reserveExtId" in {
      mockAvalancheDal.getAvalanche(any[String]) throws new RuntimeException("test RTE")
      val extIdVendor = new ExtIdVendor

      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(extIdVendor(req)())

      resp must beAnInstanceOf[InternalServerErrorResponse]
    }
  }
}
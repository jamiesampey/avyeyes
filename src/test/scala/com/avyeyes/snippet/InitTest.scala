package com.avyeyes.snippet

import com.avyeyes.data.CachedDAL
import com.avyeyes.service.{Injectors, ResourceService}
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import net.liftweb.common.{Empty, Full}
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

class InitTest extends WebSpec2 with AroundExample with Mockito {
  isolated

  val mockResources = mock[ResourceService]
  val mockAvalancheDal = mock[CachedDAL]

  mockResources.getProperty("s3.imageBucket") returns "some-bucket"

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.dal.doWith(mockAvalancheDal) {
        AsResult(t)
      }
    }

  "Initial JsCmd" >> {
    
    "Fly to an avalanche if passed a valid external ID" withSFor "/" in {
      val initAvalanche = avalancheForTest.copy(viewable = true)
      mockAvalancheDal.getAvalanche(initAvalanche.extId) returns Some(initAvalanche)

      val init = new Init
      val initJsCalls = init.initialJsCmds(Full(initAvalanche.extId)).toJsCmd

      there was one(mockAvalancheDal).getAvalanche(initAvalanche.extId)
      initJsCalls must contain("avyEyesView.addAvalancheAndFlyTo")
      autocompleteInitCallCount(initJsCalls) mustEqual 7
    }
    
    "Ignore an invalid external ID on the URL" withSFor "/" in {
      val init = new Init
      val initJsCalls = init.initialJsCmds(Full("j4ek-d3s")).toJsCmd

      there was no(mockAvalancheDal).getAvalanche(anyString)
      initJsCalls must contain("avyEyesView.geolocateAndFlyTo")
      autocompleteInitCallCount(initJsCalls) mustEqual 7
    }

    "Initialize the view without an initial avalanche" withSFor "/" in {
      val init = new Init
      val initJsCalls = init.initialJsCmds(Empty).toJsCmd

      there was no(mockAvalancheDal).getAvalanche(anyString)
      initJsCalls must contain("avyEyesView.geolocateAndFlyTo")
      autocompleteInitCallCount(initJsCalls) mustEqual 7
    }
  }
  
  private def autocompleteInitCallCount(jsStr: String) = """\$\('\.[a-zA-Z]+'\)\.avycomplete""".r.findAllMatchIn(jsStr).length
}
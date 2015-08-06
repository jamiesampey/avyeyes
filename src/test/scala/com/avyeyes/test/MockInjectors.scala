package com.avyeyes.test

import com.avyeyes.data.CachedDAL
import com.avyeyes.service.{UserSession, AmazonS3ImageService, Injectors}
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._


trait MockInjectors extends AroundExample with Mockito {
  protected lazy val mockAvalancheDal = mock[CachedDAL]
  protected lazy val mockUserSession = mock[UserSession]
  protected lazy val mockS3Service = mock[AmazonS3ImageService]

  def around[T: AsResult](t: => T): Result = Injectors.user.doWith(mockUserSession) {
    Injectors.s3.doWith(mockS3Service) {
      Injectors.dal.doWith(mockAvalancheDal) {
        AsResult(t)
      }
    }
  }
}
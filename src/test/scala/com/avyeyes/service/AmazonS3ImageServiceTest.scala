package com.avyeyes.service

import java.util.UUID

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{DeleteObjectsRequest, PutObjectRequest}
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.{AroundExample, Scope}

import scala.collection.mutable.ListBuffer

class AmazonS3ImageServiceTest extends Specification with AroundExample with Mockito {

  val mockResources = mock[ResourceService]
  val imageBucket = "my-images"
  mockResources.getProperty("s3.imageBucket") returns imageBucket
  mockResources.getProperty("s3.fullaccess.accessKeyId") returns "3490griow"
  mockResources.getProperty("s3.fullaccess.secretAccessKey") returns "34ijgeij4"

  def around[T: AsResult](t: => T): Result = Injectors.resources.doWith(mockResources) {
    AsResult(t)
  }

  class AmazonS3ImageServiceForTest(client: AmazonS3Client) extends AmazonS3ImageService {
    override val s3Client = client
    override def getAllAvalancheImageKeys(avyExtId: String) = ListBuffer(UUID.randomUUID().toString, UUID.randomUUID().toString)
  }

  class Setup extends Scope {
    val mockS3Client = mock[AmazonS3Client]
    val s3ImageService = new AmazonS3ImageServiceForTest(mockS3Client)
  }

  "Image upload" >> {
    "Makes the correct call to S3" in new Setup {
      val extId = "49d03kd2"
      val filename = UUID.randomUUID().toString
      val mimeType = "image/jpg"
      val imgBytes = Array[Byte](1, 2, 3, 4)

      val putRequestCapture = capture[PutObjectRequest]
      s3ImageService.uploadImage(extId, filename, mimeType, imgBytes)
      there was one(mockS3Client).putObject(putRequestCapture)

      putRequestCapture.value.getBucketName mustEqual imageBucket
      putRequestCapture.value.getKey mustEqual s"$extId/$filename"
      putRequestCapture.value.getMetadata.getContentType mustEqual mimeType
      putRequestCapture.value.getMetadata.getContentLength mustEqual imgBytes.length
      putRequestCapture.value.getMetadata.getCacheControl mustEqual s3ImageService.CacheControlMaxAge
    }
  }

  "Image delete" >> {
    "Makes the correct call to S3 for single image delete" in new Setup {
      val extId = "49d03kd2"
      val filename = UUID.randomUUID().toString

      val bucketCapture = capture[String]
      val keyCapture = capture[String]
      s3ImageService.deleteImage(extId, filename)
      there was one(mockS3Client).deleteObject(imageBucket, s"$extId/$filename")
    }

    "Makes the correct call to S3 to delete ALL images of an avalanche" in new Setup {
      val extId = "49d03kd2"

      val deleteObjectsRequestCapture = capture[DeleteObjectsRequest]
      s3ImageService.deleteAllImages(extId)
      there was one(mockS3Client).deleteObjects(deleteObjectsRequestCapture)

      deleteObjectsRequestCapture.value.getKeys.size() mustEqual 2
    }
  }
}
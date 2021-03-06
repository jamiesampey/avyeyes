package com.jamiesampey.avyeyes.service

import java.io.{ByteArrayOutputStream, File}
import java.util.UUID
import javax.imageio.ImageIO

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{CannedAccessControlList, DeleteObjectsRequest, PutObjectRequest}
import com.jamiesampey.avyeyes.util.Constants._
import com.jamiesampey.avyeyes.util.FutureOps._
import helpers.BaseSpec
import org.mockito.Matchers
import org.specs2.specification.Scope
import play.api.Logger

import scala.collection.JavaConversions._
import scala.util.Success

class AmazonS3ServiceTest extends BaseSpec {

  private val mockConfigService = mock[ConfigurationService]
  val s3Bucket = "test-s3-bucket"
  mockConfigService.getProperty("s3.bucket") returns s3Bucket
  mockConfigService.getProperty("s3.fullaccess.accessKeyId") returns "3490griow"
  mockConfigService.getProperty("s3.fullaccess.secretAccessKey") returns "34ijgeij4"

  class AmazonS3ServiceForTest(client: AmazonS3Client) extends AmazonS3Service(mockConfigService, mock[Logger]) {
    override val s3Client = client
    override def allAvalancheImageKeys(avyExtId: String) = Success(List(
      s"avalanches/$avyExtId/images/${UUID.randomUUID().toString}",
      s"avalanches/$avyExtId/images/${UUID.randomUUID().toString}"
    ))
    override def allAvalancheFileKeys(avyExtId: String) = Success(List(
      s"avalanches/$avyExtId/$ScreenshotFilename",
      s"avalanches/$avyExtId/$FacebookSharePageFilename",
      s"avalanches/$avyExtId/images/${UUID.randomUUID().toString}",
      s"avalanches/$avyExtId/images/${UUID.randomUUID().toString}"
    ))
  }

  class Setup extends Scope {
    val mockS3Client = mock[AmazonS3Client]
    val s3ImageService = new AmazonS3ServiceForTest(mockS3Client)
  }

  "Single image" >> {
    "Upload single image to S3" in new Setup {
      val baos = new ByteArrayOutputStream()
      ImageIO.write(ImageIO.read(new File("public/images/avyeyes.jpg")), "jpg", baos)

      val extId = "49d03kd2"
      val filename = UUID.randomUUID().toString
      val mimeType = "image/jpg"
      val imgBytes = baos.toByteArray
      baos.close

      val putRequestCapture = capture[PutObjectRequest]
      s3ImageService.uploadImage(extId, filename, mimeType, imgBytes).resolve
      there was one(mockS3Client).putObject(putRequestCapture)

      val putObjectRequest = putRequestCapture.value
      putObjectRequest.getBucketName mustEqual s3Bucket
      putObjectRequest.getKey mustEqual s"avalanches/$extId/images/$filename"
      putObjectRequest.getMetadata.getContentType mustEqual mimeType
      putObjectRequest.getMetadata.getCacheControl mustEqual s3ImageService.CacheControlMaxAge
    }

    "Delete single image from S3" in new Setup {
      val extId = "49d03kd2"
      val filename = UUID.randomUUID().toString

      val bucketCapture = capture[String]
      val keyCapture = capture[String]
      s3ImageService.deleteImage(extId, filename)
      there was one(mockS3Client).deleteObject(s3Bucket, s"avalanches/$extId/images/$filename")
    }
  }

  "File access" >> {
    "Allow public access to all files" in new Setup {
      val extId = "ir9319fk"

      val fileKeyCapture = capture[String]
      s3ImageService.allowPublicFileAccess(extId)
      there were atLeast(4)(mockS3Client).setObjectAcl(Matchers.eq(s3Bucket), fileKeyCapture, Matchers.eq(CannedAccessControlList.PublicRead))

      val fileKeyArguments = fileKeyCapture.values
      fileKeyArguments must haveLength(4)
      fileKeyArguments.head mustEqual s"avalanches/$extId/$ScreenshotFilename"
      fileKeyArguments(1) mustEqual s"avalanches/$extId/$FacebookSharePageFilename"
      fileKeyArguments.subList(2,3).forall(_.startsWith(s"avalanches/$extId/images")) must beTrue
    }

    "Allow public access to all images" in new Setup {
      val extId = "ir9319fk"

      val fileKeyCapture = capture[String]
      s3ImageService.allowPublicImageAccess(extId)
      there were two(mockS3Client).setObjectAcl(Matchers.eq(s3Bucket), fileKeyCapture, Matchers.eq(CannedAccessControlList.PublicRead))

      val fileKeyArguments = fileKeyCapture.values
      fileKeyArguments must haveLength(2)
      fileKeyArguments.forall(_.startsWith(s"avalanches/$extId/images")) must beTrue
    }

    "Deny public access to all images" in new Setup {
      val extId = "ir9319fk"

      val fileKeyCapture = capture[String]
      s3ImageService.denyPublicImageAccess(extId)
      there were two(mockS3Client).setObjectAcl(Matchers.eq(s3Bucket), fileKeyCapture, Matchers.eq(CannedAccessControlList.Private))

      val fileKeyArguments = fileKeyCapture.values
      fileKeyArguments must haveLength(2)
      fileKeyArguments.forall(_.startsWith(s"avalanches/$extId/images")) must beTrue
    }
  }

  "All files delete" >> {
    "Deletes all files associated with an avalanche from S3" in new Setup {
      val extId = "49d03kd2"

      val deleteObjectsRequestCapture = capture[DeleteObjectsRequest]
      s3ImageService.deleteAllFiles(extId)
      there was one(mockS3Client).deleteObjects(deleteObjectsRequestCapture)

      deleteObjectsRequestCapture.value.getKeys.size() mustEqual 4
    }
  }
}
package com.jamiesampey.avyeyes.service

import java.io.ByteArrayInputStream
import javax.inject.Inject

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.jamiesampey.avyeyes.util.Constants._
import play.api.Logger

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AmazonS3Service @Inject()(configService: ConfigurationService, logger: Logger) {
  private val s3Bucket = configService.getProperty("s3.bucket")

  private[service] val CacheControlMaxAge = "max-age=31536000" // 1 year in seconds

  protected val s3Client = new AmazonS3Client(new BasicAWSCredentials(
    configService.getProperty("s3.fullaccess.accessKeyId"),
    configService.getProperty("s3.fullaccess.secretAccessKey")
  ))

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  /**
    * Upload images asynchronously
    */
  def uploadImage(extId: String, filename: String, mimeType: String, imageBytes: Array[Byte]): Future[Unit] = Future {
    val key = filename match {
      case ScreenshotFilename => screenshotKey(extId)
      case _ => avalancheImageKey(extId, s"$filename")
    }

    val metadata = new ObjectMetadata()
    metadata.setContentLength(imageBytes.length)
    metadata.setContentType(mimeType)
    metadata.setCacheControl(CacheControlMaxAge)

    val uploadBais = new ByteArrayInputStream(imageBytes)
    val putObjectRequest = new PutObjectRequest(s3Bucket, key, uploadBais, metadata)

    Try(s3Client.putObject(putObjectRequest)) match {
      case Success(result) => logger.info(s"Uploaded image $key to AWS S3 in ${imageBytes.length} bytes")
      case Failure(ex) => logger.error(s"Unable to upload image $key to AWS S3", ex)
    }

    uploadBais.close
  }

  def deleteImage(avyExtId: String, filename: String) {
    val key = avalancheImageKey(avyExtId, filename)

    Try(s3Client.deleteObject(s3Bucket, key)) match {
      case Success(result) => logger.info(s"Deleted image $key from AWS S3")
      case Failure(ex) => logger.error(s"Unable to delete image $key from AWS S3", ex)
    }
  }

  def deleteAllFiles(avyExtId: String) {
    logger.info(s"Deleting all S3 files for avalanche $avyExtId")
    allAvalancheFileKeys(avyExtId).map { fileKeys =>
      val deleteObjectsRequest = new DeleteObjectsRequest(s3Bucket).withKeys(fileKeys:_*)
      Try(s3Client.deleteObjects(deleteObjectsRequest)) match {
        case Success(_) => logger.debug(s"Deleted ${fileKeys.size} files from AWS S3 for avalanche $avyExtId")
        case Failure(ex) => logger.error(s"Unable to delete files from AWS S3 for avalanche $avyExtId", ex)
      }
    }
  }

  def allowPublicFileAccess(avyExtId: String) = allAvalancheFileKeys(avyExtId).map { fileKeys =>
    fileKeys.foreach(s3Client.setObjectAcl(s3Bucket, _, CannedAccessControlList.PublicRead))
  }

  def allowPublicImageAccess(avyExtId: String) = allAvalancheImageKeys(avyExtId).map { imageKeys =>
    imageKeys.foreach(s3Client.setObjectAcl(s3Bucket, _, CannedAccessControlList.PublicRead))
  }

  def denyPublicImageAccess(avyExtId: String) = allAvalancheImageKeys(avyExtId).map { imageKeys =>
    imageKeys.foreach(s3Client.setObjectAcl(s3Bucket, _, CannedAccessControlList.Private))
  }

  def allAvalancheKeys = {
    val listObjectsRequest = new ListObjectsRequest().withBucketName(s3Bucket).withPrefix("avalanches/").withDelimiter("/")
    Try(s3Client.listObjects(listObjectsRequest).getCommonPrefixes)
  }

  private[service] def allAvalancheImageKeys(avyExtId: String) = Try(s3Client.listObjects(s3Bucket, s"${avalancheBaseKey(avyExtId)}/images").getObjectSummaries.map(_.getKey).toList)

  private[service] def allAvalancheFileKeys(avyExtId: String) = Try(s3Client.listObjects(s3Bucket, avalancheBaseKey(avyExtId))).map( _.getObjectSummaries.map(_.getKey).toList)

  private def avalancheImageKey(avyExtId: String, filename: String) = s"${avalancheBaseKey(avyExtId)}/images/$filename"

  private def screenshotKey(avyExtId: String) = s"${avalancheBaseKey(avyExtId)}/$ScreenshotFilename"

  private def facebookSharePageKey(avyExtId: String) = s"${avalancheBaseKey(avyExtId)}/$FacebookSharePageFilename"

  private def avalancheBaseKey(avyExtId: String) = s"avalanches/$avyExtId"
}

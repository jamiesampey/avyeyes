package com.avyeyes.service

import java.io.ByteArrayInputStream

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.avyeyes.model.Avalanche
import com.avyeyes.util.Constants._
import net.liftweb.common.Loggable

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

class AmazonS3Service extends Loggable {
  val R = Injectors.resources.vend
  private val s3Bucket = R.getProperty("s3.bucket")

  private[service] val CacheControlMaxAge = "max-age=31536000" // 1 year in seconds

  protected val s3Client = new AmazonS3Client(new BasicAWSCredentials(
    R.getProperty("s3.fullaccess.accessKeyId"),
    R.getProperty("s3.fullaccess.secretAccessKey")
  ))

  def uploadImage(avyExtId: String, filename: String, mimeType: String, bytes: Array[Byte]) {
    val key = s3ImageKey(avyExtId, Some(filename))

    val metadata = new ObjectMetadata()
    metadata.setContentLength(bytes.length)
    metadata.setContentType(mimeType)
    metadata.setCacheControl(CacheControlMaxAge)

    val putObjectRequest = new PutObjectRequest(s3Bucket, key, new ByteArrayInputStream(bytes), metadata)

    Try(s3Client.putObject(putObjectRequest)) match {
      case Success(result) => logger.info(s"Uploaded image $key to AWS S3 in ${bytes.length} bytes")
      case Failure(ex) => logger.error(s"Unable to upload image $key to AWS S3", ex)
    }
  }

  def deleteImage(avyExtId: String, filename: String) {
    val key = s3ImageKey(avyExtId, Some(filename))

    Try(s3Client.deleteObject(s3Bucket, key)) match {
      case Success(result) => logger.info(s"Deleted image $key from AWS S3")
      case Failure(ex) => logger.error(s"Unable to delete image $key from AWS S3", ex)
    }
  }

  def allowPublicFileAccess(avyExtId: String) = Try(s3Client.listObjects(s3Bucket, s"avalanches/$avyExtId")).map( _.getObjectSummaries.map(_.getKey).foreach { fileKey =>
    s3Client.setObjectAcl(s3Bucket, fileKey, CannedAccessControlList.PublicRead)
  })

  def allowPublicImageAccess(avyExtId: String) = getAllAvalancheImageKeys(avyExtId).map { imageKeys =>
    imageKeys.foreach(s3Client.setObjectAcl(s3Bucket, _, CannedAccessControlList.PublicRead))
  }

  def denyPublicImageAccess(avyExtId: String) = getAllAvalancheImageKeys(avyExtId).map { imageKeys =>
    imageKeys.foreach(s3Client.setObjectAcl(s3Bucket, _, CannedAccessControlList.Private))
  }

  def uploadFacebookSharePage(avalanche: Avalanche) = {
    val html =
      <html>
        <head>
          <meta property="og:url" content={R.avalancheUrl(avalanche.extId)} />
          <meta property="og:type" content="article" />
          <meta property="og:title" content="AvyEyes" />
          <meta property="og:description" content={avalanche.title} />
          <meta property="og:image" content={s3Client.getResourceUrl(s3Bucket, s3ImageKey(avalanche.extId, Some(ScreenshotFilename)))} />
          <meta http-equiv="refresh" content={s"0; url=${R.avalancheUrl(avalanche.extId)}"} />
        </head>
      </html>

    val bytes = html.toString.getBytes("UTF-8")
    val fbSharePageKey = s3FacebookSharePageKey(avalanche.extId)

    val metadata = new ObjectMetadata()
    metadata.setContentLength(bytes.length)
    metadata.setContentType("text/html")
    metadata.setCacheControl(CacheControlMaxAge)

    val putObjectRequest = new PutObjectRequest(s3Bucket, fbSharePageKey, new ByteArrayInputStream(bytes), metadata)

    Try(s3Client.putObject(putObjectRequest)) match {
      case Success(result) =>
        s3Client.setObjectAcl(s3Bucket, fbSharePageKey, CannedAccessControlList.PublicRead)
        logger.info(s"Successfully uploaded facebook share page for avalanche ${avalanche.extId}")
      case Failure(ex) => logger.error(s"Failed to upload a facebook share page for avalanche ${avalanche.extId}")
    }
  }

  def deleteAllFiles(avyExtId: String) {
    logger.info(s"Deleting all S3 files for avalanche $avyExtId")
    Try(s3Client.listObjects(s3Bucket, s"avalanches/$avyExtId")).map( _.getObjectSummaries.map(_.getKey).foreach { objectKey =>
      Try(s3Client.deleteObject(s3Bucket, objectKey)) match {
        case Success(result) => logger.debug(s"Deleted $objectKey from AWS S3")
        case Failure(ex) => logger.error(s"Unable to delete $objectKey from AWS S3", ex)
      }
    })
  }

  private def getAllAvalancheImageKeys(avyExtId: String) = Try(s3Client.listObjects(s3Bucket, s3ImageKey(avyExtId)).getObjectSummaries.map(_.getKey))

  private def s3FacebookSharePageKey(avyExtId: String) = s"avalanches/$avyExtId/$FacebookSharePageFilename"

  private def s3ImageKey(avyExtId: String, filenameOpt: Option[String] = None) = filenameOpt match {
    case Some(filename) if filename == ScreenshotFilename => s"avalanches/$avyExtId/$ScreenshotFilename"
    case Some(filename) => s"avalanches/$avyExtId/images/$filename"
    case None => s"avalanches/$avyExtId/images"
  }
}

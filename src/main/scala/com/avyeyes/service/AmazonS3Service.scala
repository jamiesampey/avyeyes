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
  private val s3FilesBucket = R.getProperty("s3.filesBucket")

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

    val putObjectRequest = new PutObjectRequest(s3FilesBucket, key, new ByteArrayInputStream(bytes), metadata)

    Try(s3Client.putObject(putObjectRequest)) match {
      case Success(result) => logger.info(s"Uploaded image $key to AWS S3")
      case Failure(ex) => logger.error(s"Unable to upload image $key to AWS S3", ex)
    }
  }

  def deleteImage(avyExtId: String, filename: String) {
    val key = s3ImageKey(avyExtId, Some(filename))

    Try(s3Client.deleteObject(s3FilesBucket, key)) match {
      case Success(result) => logger.info(s"Deleted image $key from AWS S3")
      case Failure(ex) => logger.error(s"Unable to delete image $key from AWS S3", ex)
    }
  }

  def deleteAllImages(avyExtId: String) = getAllAvalancheImageKeys(avyExtId).map { imageKeys =>
    val deleteObjectsRequest = new DeleteObjectsRequest(s3FilesBucket)
    deleteObjectsRequest.withKeys(imageKeys:_*)

    Try(s3Client.deleteObjects(deleteObjectsRequest)) match {
      case Success(result) => logger.info(s"Deleted all ${imageKeys.size} images for avalanche $avyExtId from AWS S3")
      case Failure(ex) => logger.error(s"Unable to delete ALL images for avalanche $avyExtId from AWS S3", ex)
    }
  }

  def allowPublicImageAccess(avyExtId: String) = getAllAvalancheImageKeys(avyExtId).map { imageKeys =>
    imageKeys.foreach(s3Client.setObjectAcl(s3FilesBucket, _, CannedAccessControlList.PublicRead))
  }

  def denyPublicImageAccess(avyExtId: String) = getAllAvalancheImageKeys(avyExtId).map { imageKeys =>
    imageKeys.foreach(s3Client.setObjectAcl(s3FilesBucket, _, CannedAccessControlList.Private))
  }

  def uploadFacebookSharePage(avalanche: Avalanche) = {
    val html =
      <html>
        <head>
          <meta property="og:type" content="article" />
          <meta property="og:title" content="AvyEyes" />
          <meta property="og:description" content={s"${avalanche.areaName}"} />
          <meta property="og:image" content={s"${s3Client.getResourceUrl(s3FilesBucket, s3ImageKey(avalanche.extId, Some(ScreenshotFilename)))}"} />
          <meta http-equiv="refresh" content={s"0; url=${R.avalancheUrl(avalanche.extId)}"} />
        </head>
      </html>

    val bytes = html.toString.getBytes("UTF-8")
    val fbSharePageKey = s3FacebookSharePageKey(avalanche.extId)

    val metadata = new ObjectMetadata()
    metadata.setContentLength(bytes.length)
    metadata.setContentType("text/html")
    metadata.setCacheControl(CacheControlMaxAge)

    val putObjectRequest = new PutObjectRequest(s3FilesBucket, fbSharePageKey, new ByteArrayInputStream(bytes), metadata)

    Try(s3Client.putObject(putObjectRequest)) match {
      case Success(result) =>
        s3Client.setObjectAcl(s3FilesBucket, fbSharePageKey, CannedAccessControlList.PublicRead)
        logger.info(s"Successfully uploaded facebook share page for avalanche ${avalanche.extId}")
      case Failure(ex) => logger.error(s"Failed to upload a facebook share page for avalanche ${avalanche.extId}")
    }
  }

  def deleteAllFiles(avyExtId: String) {
    deleteAllImages(avyExtId)

    val fbSharePageKey = s3FacebookSharePageKey(avyExtId)
    Try(s3Client.deleteObject(s3FilesBucket, fbSharePageKey)) match {
      case Success(result) => logger.info(s"Deleted image $fbSharePageKey from AWS S3")
      case Failure(ex) => logger.error(s"Unable to delete image $fbSharePageKey from AWS S3", ex)
    }
  }

  private def getAllAvalancheImageKeys(avyExtId: String) = Try(s3Client.listObjects(s3FilesBucket, s3ImageKey(avyExtId)).getObjectSummaries.map(_.getKey))

  private def s3FacebookSharePageKey(avyExtId: String) = s"facebook/$avyExtId.html"

  private def s3ImageKey(avyExtId: String, filenameOpt: Option[String] = None) = filenameOpt match {
    case Some(filename) => s"images/$avyExtId/$filename"
    case None => s"images/$avyExtId"
  }
}

package com.avyeyes.service

import java.io.ByteArrayInputStream

import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.avyeyes.model.Avalanche
import com.avyeyes.util.Constants._
import net.liftweb.common.Loggable

import scala.collection.JavaConversions._

class AmazonS3Service extends Loggable {
  val R = Injectors.resources.vend
  private val s3FilesBucket = R.getProperty("s3.filesBucket")

  private[service] val CacheControlMaxAge = "max-age=31536000" // 1 year in seconds

  protected val s3Client = new AmazonS3Client(new BasicAWSCredentials(
    R.getProperty("s3.fullaccess.accessKeyId"),
    R.getProperty("s3.fullaccess.secretAccessKey")
  ))

  def uploadImage(avyExtId: String, filename: String, mimeType: String, bytes: Array[Byte]) {
    val key = toS3ImageKey(avyExtId, filename)
    try {
      val metadata = new ObjectMetadata()
      metadata.setContentLength(bytes.length)
      metadata.setContentType(mimeType)
      metadata.setCacheControl(CacheControlMaxAge)

      val putObjectRequest = new PutObjectRequest(s3FilesBucket, key,
        new ByteArrayInputStream(bytes), metadata)
      s3Client.putObject(putObjectRequest)

      logger.info(s"Uploaded image $key to AWS S3")
    } catch {
      case ase: AmazonServiceException => logger.error(s"Unable to upload image $key to AWS S3", ase)
    }
  }

  def deleteImage(avyExtId: String, filename: String) {
    val key = toS3ImageKey(avyExtId, filename)
    try {
      s3Client.deleteObject(s3FilesBucket, key)
      logger.info(s"Deleted image $key from AWS S3")
    } catch {
      case ase: AmazonServiceException => logger.error(s"Unable to delete image $key from AWS S3", ase)
    }
  }

  def deleteAllImages(avyExtId: String) {
    try {
      getAllAvalancheImageKeys(avyExtId).toList match {
        case Nil => logger.info(s"No images to delete for avalanche $avyExtId")
        case imageKeys =>
          val deleteObjectsRequest = new DeleteObjectsRequest(s3FilesBucket)
          deleteObjectsRequest.withKeys(imageKeys:_*)
          s3Client.deleteObjects(deleteObjectsRequest)

          logger.info(s"Deleted all ${imageKeys.size} images for avalanche $avyExtId from AWS S3")
      }
    } catch {
      case ase: AmazonServiceException => logger.error(s"Unable to delete ALL images for avalanche $avyExtId from AWS S3", ase)
    }
  }

  def allowPublicImageAccess(avyExtId: String) {
    getAllAvalancheImageKeys(avyExtId).foreach(s3Client.setObjectAcl(s3FilesBucket, _,
      CannedAccessControlList.PublicRead))
  }

  def denyPublicImageAccess(avyExtId: String) {
    getAllAvalancheImageKeys(avyExtId).foreach(s3Client.setObjectAcl(s3FilesBucket, _,
      CannedAccessControlList.Private))
  }

  private def toS3ImageKey(avyExtId: String, filename: String) = s"images/$avyExtId/$filename"

  protected def getAllAvalancheImageKeys(avyExtId: String) =
    s3Client.listObjects(s3FilesBucket, s"images/$avyExtId").getObjectSummaries.map(_.getKey)

  def uploadFacebookSharePage(avalanche: Avalanche) = {
    val html =
      <html>
        <head>
          <meta property="og:type" content="article" />
          <meta property="og:title" content="AvyEyes" />
          <meta property="og:description" content={s"${avalanche.areaName}"} />
          <meta property="og:image" content={s"${s3Client.getResourceUrl(s3FilesBucket, toS3ImageKey(avalanche.extId, ScreenshotFilename))}"} />
          <meta http-equiv="refresh" content={s"0; url=${R.avalancheUrl(avalanche.extId)}"} />
        </head>
      </html>

    val bytes = html.toString.getBytes("UTF-8")
    val fbSharePageKey = s"facebook/${avalanche.extId}.html"

    val metadata = new ObjectMetadata()
    metadata.setContentLength(bytes.length)
    metadata.setContentType("text/html")
    metadata.setCacheControl(CacheControlMaxAge)

    val putObjectRequest = new PutObjectRequest(s3FilesBucket, fbSharePageKey, new ByteArrayInputStream(bytes), metadata)
    s3Client.putObject(putObjectRequest)
    s3Client.setObjectAcl(s3FilesBucket, fbSharePageKey, CannedAccessControlList.PublicRead)
  }
}

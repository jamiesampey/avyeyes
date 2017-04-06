package com.avyeyes.service

import java.awt.geom.AffineTransform
import java.io.ByteArrayInputStream

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.avyeyes.model.Avalanche
import com.avyeyes.util.Constants._
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.{ExifDirectoryBase, ExifIFD0Directory}
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
    getExifTransformation(bytes)

    val key = filename match {
      case ScreenshotFilename => screenshotKey(avyExtId)
      case _ => avalancheImageKey (avyExtId, filename)
    }

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

  private def getExifTransformation(imageBytes: Array[Byte]): Unit = {
    val bais = new ByteArrayInputStream(imageBytes)
    val metadata: Metadata = ImageMetadataReader.readMetadata(bais)

    val orientationOption = Option(metadata.getFirstDirectoryOfType(classOf[ExifIFD0Directory])).map { exifIFO0Dir =>
      exifIFO0Dir.getInt(ExifDirectoryBase.TAG_ORIENTATION)
    }

    logger.debug(s"Orientation is: $orientationOption")

  }

  private def rotationTransform(orientation: Int) = {
    val transform = new AffineTransform
    orientation match {
      case 2 => // Flip X
        transform.scale(-1.0, 1.0)
        transform.translate(info.width, 0)
      case 3 => // PI rotation
        transform.translate(info.width, info.height)
        transform.rotate(Math.PI)
      case 4 => // Flip Y
        transform.scale(1.0, -1.0)
        transform.translate(0, -info.height)
      case 5 => // - PI/2 and Flip X
        transform.rotate(-Math.PI / 2)
        transform.scale(-1.0, 1.0)
      case 6 => // -PI/2 and -width
        transform.translate(info.height, 0)
        transform.rotate(Math.PI / 2)
      case 7 => // PI/2 and Flip
        transform.scale(-1.0, 1.0)
        transform.translate(-info.height, 0)
        transform.translate(0, info.width)
        transform.rotate(3 * Math.PI / 2)
      case 8 => // PI / 2
        transform.translate(0, info.width)
        transform.rotate(3 * Math.PI / 2)
    }
  }
}

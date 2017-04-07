package com.avyeyes.service

import java.awt.geom.AffineTransform
import java.awt.image.{AffineTransformOp, BufferedImage}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.avyeyes.util.Constants._
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.{Directory, Metadata}
import com.drew.metadata.exif.{ExifDirectoryBase, ExifIFD0Directory}
import com.drew.metadata.jpeg.JpegDirectory
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

  def uploadImage(avyExtId: String, filename: String, mimeType: String, origBytes: Array[Byte]) {
    val bytesForUpload = exifBasedTransform(filename, mimeType, origBytes)

    val key = filename match {
      case ScreenshotFilename => screenshotKey(avyExtId)
      case _ => avalancheImageKey (avyExtId, filename)
    }

    val metadata = new ObjectMetadata()
    metadata.setContentLength(bytesForUpload.length)
    metadata.setContentType(mimeType)
    metadata.setCacheControl(CacheControlMaxAge)

    val bais = new ByteArrayInputStream(bytesForUpload)
    val putObjectRequest = new PutObjectRequest(s3Bucket, key, bais, metadata)

    Try(s3Client.putObject(putObjectRequest)) match {
      case Success(result) => logger.info(s"Uploaded image $key to AWS S3 in ${bytesForUpload.length} bytes")
      case Failure(ex) => logger.error(s"Unable to upload image $key to AWS S3", ex)
    }

    bais.close
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

  private def exifBasedTransform(filename: String, mimeType: String, origImageBytes: Array[Byte]): Array[Byte] = {
    def exifTagValueOption(exifDir: Directory, tag: Int) = Try(exifDir.getInt(tag)) match {
      case Success(tagValue) => Some(tagValue)
      case Failure(_) => None
    }

    val origBais = new ByteArrayInputStream(origImageBytes)
    val metadata: Metadata = ImageMetadataReader.readMetadata(origBais)

    val transformedBytes: Option[Array[Byte]] = Option(metadata.getFirstDirectoryOfType(classOf[ExifIFD0Directory])).flatMap { exifIFO0Dir =>
      exifTagValueOption(exifIFO0Dir, ExifDirectoryBase.TAG_ORIENTATION)
    }.flatMap { orientation =>
      val jpegDirectoryOpt = Option(metadata.getFirstDirectoryOfType(classOf[JpegDirectory]))
      val widthOpt = jpegDirectoryOpt.flatMap(jpegDirectory => exifTagValueOption(jpegDirectory, JpegDirectory.TAG_IMAGE_WIDTH))
      val heightOpt = jpegDirectoryOpt.flatMap(jpegDirectory => exifTagValueOption(jpegDirectory, JpegDirectory.TAG_IMAGE_HEIGHT))

      (widthOpt, heightOpt) match {
        case (Some(width), Some(height)) =>
          logger.debug(s"Transforming image $filename based on orientation($orientation), width($width), height($height)")

          val transform = rotationTransform(orientation, width, height)
          val transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC)

          val origImage = ImageIO.read(origBais)
          val destImage = transformOp.createCompatibleDestImage(origImage, if (origImage.getType == BufferedImage.TYPE_BYTE_GRAY) origImage.getColorModel else null)
          val baos = new ByteArrayOutputStream()
          ImageIO.write(destImage, mimeType, baos)
          val transformedBytes = baos.toByteArray
          baos.close
          Some(transformedBytes)
        case _ =>
          logger.warn(s"Tried to transform image $filename but a necessary tag could not be retrieved: orientation($orientation), width($widthOpt), height($heightOpt)")
          None
      }
    }

    origBais.close
    transformedBytes.getOrElse(origImageBytes)
  }

  private def rotationTransform(orientation: Int, width: Int, height: Int): AffineTransform = {
    val transform = new AffineTransform

    orientation match {
      case 2 => // Flip X
        transform.scale(-1.0, 1.0)
        transform.translate(width, 0)
      case 3 => // PI rotation
        transform.translate(width, height)
        transform.rotate(Math.PI)
      case 4 => // Flip Y
        transform.scale(1.0, -1.0)
        transform.translate(0, -height)
      case 5 => // - PI/2 and Flip X
        transform.rotate(-Math.PI / 2)
        transform.scale(-1.0, 1.0)
      case 6 => // -PI/2 and -width
        transform.translate(height, 0)
        transform.rotate(Math.PI / 2)
      case 7 => // PI/2 and Flip
        transform.scale(-1.0, 1.0)
        transform.translate(-height, 0)
        transform.translate(0, width)
        transform.rotate(3 * Math.PI / 2)
      case 8 => // PI / 2
        transform.translate(0, width)
        transform.rotate(3 * Math.PI / 2)
    }

    transform
  }
}

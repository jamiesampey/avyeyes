package com.avyeyes.service

import java.io.ByteArrayInputStream

import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import net.liftweb.common.Loggable
import scala.collection.JavaConversions._

class AmazonS3ImageService extends Loggable {
  val R = Injectors.resources.vend

  private val s3ImageBucket = R.getProperty("s3.imageBucket")

  protected val s3Client = new AmazonS3Client(new BasicAWSCredentials(
    R.getProperty("s3.fullaccess.accessKeyId"),
    R.getProperty("s3.fullaccess.secretAccessKey")
  ))

  def uploadImage(avyExtId: String, filename: String, mimeType: String, bytes: Array[Byte]) {
    val key = toS3Key(avyExtId, filename)
    try {
      val metadata = new ObjectMetadata()
      metadata.setContentLength(bytes.length)
      metadata.setContentType(mimeType)

      val putObjectRequest = new PutObjectRequest(s3ImageBucket, key,
        new ByteArrayInputStream(bytes), metadata)
      s3Client.putObject(putObjectRequest)

      logger.info(s"Uploaded image $key to AWS S3")
    } catch {
      case ase: AmazonServiceException => logger.error(s"Unable to upload image $key to AWS S3", ase)
    }
  }

  def deleteImage(avyExtId: String, filename: String) {
    val key = toS3Key(avyExtId, filename)
    try {
      s3Client.deleteObject(s3ImageBucket, key)
      logger.info(s"Deleted image $key from AWS S3")
    } catch {
      case ase: AmazonServiceException => logger.error(s"Unable to delete image $key from AWS S3", ase)
    }
  }

  def deleteAllImages(avyExtId: String) {
    try {
      val imageKeyList = getAllAvalancheImageKeys(avyExtId)

      val deleteObjectsRequest = new DeleteObjectsRequest(s3ImageBucket)
      deleteObjectsRequest.withKeys(imageKeyList:_*)
      s3Client.deleteObjects(deleteObjectsRequest)

      logger.info(s"Deleted all ${imageKeyList.size} images for avalanche $avyExtId from AWS S3")
    } catch {
      case ase: AmazonServiceException => logger.error(s"Unable to delete ALL images for avalanche $avyExtId from AWS S3", ase)
    }
  }

  def allowPublicImageAccess(avyExtId: String) {
    getAllAvalancheImageKeys(avyExtId).foreach(s3Client.setObjectAcl(s3ImageBucket, _,
      CannedAccessControlList.PublicRead))
  }

  def denyPublicImageAccess(avyExtId: String) {
    getAllAvalancheImageKeys(avyExtId).foreach(s3Client.setObjectAcl(s3ImageBucket, _,
      CannedAccessControlList.Private))
  }

  private def toS3Key(avyExtId: String, filename: String) = s"$avyExtId/$filename"

  protected def getAllAvalancheImageKeys(avyExtId: String) =
    s3Client.listObjects(s3ImageBucket, avyExtId).getObjectSummaries.map(_.getKey)
}

package com.avyeyes.controllers

import java.io.{ByteArrayInputStream, FileInputStream}
import java.util.UUID
import javax.inject.Inject

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.AvalancheImage
import com.avyeyes.service.{AmazonS3Service, ConfigurationService}
import com.avyeyes.system.UserEnvironment
import com.avyeyes.util.Constants.{MaxImagesPerAvalanche, ScreenshotFilename}
import com.sksamuel.scrimage.nio.{GifWriter, JpegWriter, PngWriter}
import com.sksamuel.scrimage.{Format, FormatDetector, Image}
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.json4s.JsonDSL._
import org.json4s.{JArray, JString}
import play.api.Logger
import securesocial.core.SecureSocial

import scala.concurrent.Future


class ImageController @Inject()(dal: CachedDAL, s3: AmazonS3Service, authorizations: Authorizations, logger: Logger, val configService: ConfigurationService, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods {

  import authorizations._

  private val JpegMimeType = "image/jpeg"
  private val PngMimeType = "image/png"
  private val GifMimeType = "image/gif"

  def upload(extId: String, editKeyOpt: Option[String]) = UserAwareAction.async(parse.temporaryFile) { implicit request =>
    if (!isAuthorizedToEdit(extId, request.user, editKeyOpt)) {
      logger.warn(s"Not authorized to add images to avalanche $extId")
      Future { Unauthorized }
    } else dal.countAvalancheImages(extId).flatMap {

      case siblingImageCount if siblingImageCount >= MaxImagesPerAvalanche =>
        logger.warn(s"Cannot add more images to avalanche $extId")
        Future { BadRequest }

      case siblingImageCount =>
        val imageFile = request.body.file
        val imageBytes = IOUtils.toByteArray(new FileInputStream(imageFile))

        val (writer, mimeType) = FormatDetector.detect(imageBytes) match {
          case Some(Format.PNG) => (PngWriter(), PngMimeType)
          case Some(Format.GIF) => (GifWriter(), GifMimeType)
          case _ => (JpegWriter(), JpegMimeType)
        }
        implicit val imageWriter = writer

        val origBais = new ByteArrayInputStream(imageBytes)
        val rewrittenBytes = Image.fromStream(origBais).forWriter(writer).bytes

        val newFilename = s"${UUID.randomUUID().toString}.${imageFile.getName.split('.').last.toLowerCase}"
        logger.trace(s"Uploading image $newFilename (originally ${imageFile.getName}) for avalanche $extId")

        s3.uploadImage(extId, newFilename, mimeType, rewrittenBytes).flatMap { _ =>
          dal.insertAvalancheImage(AvalancheImage(
            createTime = DateTime.now,
            avalanche = extId,
            filename = newFilename,
            origFilename = imageFile.getName,
            mimeType = mimeType,
            size = rewrittenBytes.length,
            sortOrder = siblingImageCount
          )).map { _ =>
            dal.getAvalanche(extId).foreach { case avalanche if avalanche.viewable => s3.allowPublicImageAccess(extId) }
            Ok(writeJson(("extId" -> extId) ~ ("filename" -> newFilename) ~ ("origFilename" -> imageFile.getName) ~ ("size" -> rewrittenBytes.length)))
          }
        }
    }
  }

  def uploadScreenshot(avyExtId: String) = UserAwareAction.async(parse.temporaryFile) { implicit request =>
    if (!isAuthorizedToEdit(avyExtId, request.user, None)) {
      logger.warn(s"Not authorized to add screenshot to avalanche $avyExtId")
      Future { Unauthorized }
    } else {
      val imageBytes = IOUtils.toByteArray(new FileInputStream(request.body.file))
      logger.info(s"Uploading screenshot for new avalanche $avyExtId")
      s3.uploadImage(avyExtId, ScreenshotFilename, JpegMimeType, imageBytes).map(_ => Ok)
    }
  }

  def order(avyExtId: String, editKeyOpt: Option[String]) = UserAwareAction { implicit request =>
    if (!isAuthorizedToEdit(avyExtId, request.user, editKeyOpt)) {
      logger.warn(s"Not authorized to edit image order for avalanche $avyExtId")
      Unauthorized
    } else readJson(request.body.asText) \ "order" match {
      case JArray(order) =>
        dal.updateAvalancheImageOrder(avyExtId, order.map(_.extract[String]))
        logger.debug(s"Successfully set image order on avalanche $avyExtId")
        Ok
      case _ =>
        logger.error("Received an image order PUT request, but the order payload was missing")
        BadRequest
    }
  }

  def caption(avyExtId: String, baseFilename: String, editKeyOpt: Option[String]) = UserAwareAction { implicit request =>
    if (!isAuthorizedToEdit(avyExtId, request.user, editKeyOpt)) {
      logger.warn(s"Not authorized to edit image caption for avalanche $avyExtId")
      Unauthorized
    } else readJson(request.body.asText) \ "caption" match {
      case JString(caption) if caption.nonEmpty =>
        dal.updateAvalancheImageCaption(avyExtId, baseFilename, Some(caption))
        logger.debug(s"Successfully set caption on $avyExtId/$baseFilename")
        Ok
      case JString("") =>
        dal.updateAvalancheImageCaption(avyExtId, baseFilename, None)
        logger.debug(s"Successfully cleared caption on $avyExtId/$baseFilename")
        Ok
      case _ =>
        logger.error("Received an image caption PUT request, but the caption payload was missing")
        BadRequest
    }
  }

  def delete(avyExtId: String, baseFilename: String, editKeyOpt: Option[String]) = UserAwareAction.async { implicit request =>
    if (!isAuthorizedToEdit(avyExtId, request.user, editKeyOpt)) {
      logger.warn(s"Not authorized to delete image for avalanche $avyExtId")
      Future { Unauthorized }
    } else dal.getAvalancheImage(avyExtId, baseFilename).flatMap {
        case Some(image) =>
          s3.deleteImage(avyExtId, image.filename)
          dal.deleteAvalancheImage(avyExtId, image.filename)
        case None => Future.failed(new RuntimeException("Couldn't find image"))
      }.map { _ =>
        logger.debug(s"Successfully deleted image $avyExtId/$baseFilename")
        Ok
      }.recover {
        case ex: Exception =>
          logger.error(s"Error while attempting to delete image $avyExtId/$baseFilename", ex)
          InternalServerError
      }
  }

  private def detectFormat(imageBytes: Array[Byte]): (Format, String) = FormatDetector.detect(imageBytes) match {
    case Some(Format.PNG) => (Format.PNG, "image/png")
    case Some(Format.GIF) => (Format.GIF, "image/gif")
    case _ => (Format.JPEG, "image/jpeg")
  }
}

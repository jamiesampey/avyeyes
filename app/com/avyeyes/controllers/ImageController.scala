package com.avyeyes.controllers

import java.io.FileInputStream
import java.util.UUID
import javax.inject.Inject

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.{AvalancheImage, AvyEyesUser}
import com.avyeyes.service.{AmazonS3Service, ConfigurationService}
import com.avyeyes.system.UserEnvironment
import com.avyeyes.util.Constants.{MaxImagesPerAvalanche, ScreenshotFilename}
import com.sksamuel.scrimage.{Format, FormatDetector}
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.json4s.JsonDSL._
import org.json4s.{JArray, JString}
import play.api.Logger
import play.api.libs.Files
import play.api.mvc.Result
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.RequestWithUser

import scala.concurrent.Future


class ImageController @Inject()(dal: CachedDAL, s3: AmazonS3Service, authorizations: Authorizations, logger: Logger, val configService: ConfigurationService, implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods {

  import authorizations._

  def upload(extId: String, editKeyOpt: Option[String]) = UserAwareAction.async(parse.temporaryFile) { implicit request =>
    tryImageUpload(extId, editKeyOpt)
  }

  def uploadScreenshot(avyExtId: String) = UserAwareAction.async(parse.temporaryFile) { implicit request =>
    if (!isAuthorizedToEdit(avyExtId, request.user, None)) {
      logger.warn(s"Not authorized to add screenshot to avalanche $avyExtId")
      Future { Unauthorized }
    } else {
      val imageFile = request.body.file
      val imageBytes = IOUtils.toByteArray(new FileInputStream(imageFile))
      val (imageFormat, mimeType) = detectFormat(imageBytes)

      logger.info(s"Uploading screenshot for new avalanche $avyExtId")
      s3.uploadImage(avyExtId, ScreenshotFilename, imageBytes, imageFormat, mimeType).map(_ => Ok)
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

  private[controllers] def tryImageUpload(avyExtId: String, editKeyOpt: Option[String])(implicit request: RequestWithUser[Files.TemporaryFile, AvyEyesUser]): Future[Result] = {
    if (!isAuthorizedToEdit(avyExtId, request.user, editKeyOpt)) {
      logger.warn(s"Not authorized to add images to avalanche $avyExtId")
      Future { Unauthorized }
    } else dal.countAvalancheImages(avyExtId).flatMap {

      case siblingImageCount if siblingImageCount >= MaxImagesPerAvalanche =>
        logger.warn(s"Cannot add more images to avalanche $avyExtId")
        Future { BadRequest }

      case siblingImageCount =>
        val imageFile = request.body.file
        val imageBytes = IOUtils.toByteArray(new FileInputStream(imageFile))
        val (imageFormat, mimeType) = detectFormat(imageBytes)
        val newFilename = s"${UUID.randomUUID().toString}.${imageFile.getName.split('.').last.toLowerCase}"

        logger.trace(s"Uploading image $newFilename (${imageFile.getName}) for avalanche $avyExtId")

        s3.uploadImage(avyExtId, newFilename, imageBytes, imageFormat, mimeType).flatMap { _ =>
          dal.insertAvalancheImage(AvalancheImage(
            createTime = DateTime.now,
            avalanche = avyExtId,
            filename = newFilename,
            origFilename = imageFile.getName,
            mimeType = mimeType,
            size = imageBytes.length,
            sortOrder = siblingImageCount
          )).map { _ =>
            dal.getAvalanche(avyExtId).foreach { case avalanche if avalanche.viewable => s3.allowPublicImageAccess(avyExtId) }
            Ok(writeJson(("extId" -> avyExtId) ~ ("filename" -> newFilename) ~ ("origFilename" -> imageFile.getName) ~ ("size" -> imageBytes.length)))
          }
        }
    }
  }

  private def detectFormat(imageBytes: Array[Byte]): (Format, String) = FormatDetector.detect(imageBytes) match {
    case Some(Format.PNG) => (Format.PNG, "image/png")
    case Some(Format.GIF) => (Format.GIF, "image/gif")
    case _ => (Format.JPEG, "image/jpeg")
  }
}

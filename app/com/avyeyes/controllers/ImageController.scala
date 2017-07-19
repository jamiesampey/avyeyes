package com.avyeyes.controllers

import java.io.{ByteArrayInputStream, File, FileInputStream}
import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.avyeyes.data.CachedDao
import com.avyeyes.model.{AvalancheImage, AvyEyesUser}
import com.avyeyes.service.{AmazonS3Service, ConfigurationService}
import com.avyeyes.system.UserEnvironment
import com.avyeyes.util.Constants.{MaxImagesPerAvalanche, ScreenshotFilename, ScreenshotRequestFilename}
import com.sksamuel.scrimage.nio.{GifWriter, JpegWriter, PngWriter}
import com.sksamuel.scrimage.{Format, FormatDetector, Image}
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import play.api.Logger
import play.api.libs.json.{JsArray, JsString}
import play.api.mvc.Result
import securesocial.core.SecureSocial

import scala.concurrent.Future

@Singleton
class ImageController @Inject()(dao: CachedDao, s3: AmazonS3Service, authorizations: Authorizations,
                                logger: Logger, val configService: ConfigurationService,
                                implicit val env: UserEnvironment)
  extends SecureSocial with Json4sMethods {

  import authorizations._

  private[controllers] val JpegMimeType = "image/jpeg"
  private[controllers] val PngMimeType = "image/png"
  private[controllers] val GifMimeType = "image/gif"

  def uploadImages(extId: String, editKeyOpt: Option[String]) = UserAwareAction.async(parse.multipartFormData) { implicit request =>
    doImagesUpload(extId, editKeyOpt, request.user, request.body.files.map(f => (f.filename, f.ref.file)))
  }

  private[controllers] def doImagesUpload(extId: String, editKeyOpt: Option[String], user: Option[AvyEyesUser], files: Seq[(String, File)]) = {
    if (!isAuthorizedToEdit(extId, user, editKeyOpt)) {
      logger.warn(s"Not authorized to add images to avalanche $extId")
      Future { Unauthorized }
    } else dao.countAvalancheImages(extId).flatMap {

      case siblingImageCount if siblingImageCount >= MaxImagesPerAvalanche =>
        logger.warn(s"Cannot add more images to avalanche $extId")
        Future { BadRequest }

      case siblingImageCount =>

        val jValueFutures: Seq[Future[JValue]] = files.map { case (origFilename, file) =>
          val imageBytes = IOUtils.toByteArray(new FileInputStream(file))

          val (writer, mimeType) = FormatDetector.detect(imageBytes) match {
            case Some(Format.PNG) => (PngWriter(), PngMimeType)
            case Some(Format.GIF) => (GifWriter(), GifMimeType)
            case _ => (JpegWriter(), JpegMimeType)
          }
          implicit val imageWriter = writer

          val origBais = new ByteArrayInputStream(imageBytes)
          val rewrittenBytes = Image.fromStream(origBais).forWriter(writer).bytes

          val newFilename = s"${UUID.randomUUID().toString}.${origFilename.split('.').last.toLowerCase}"
          logger.debug(s"Uploading image $newFilename (originally $origFilename) for avalanche $extId")

          s3.uploadImage(extId, newFilename, mimeType, rewrittenBytes).flatMap { _ =>
            dao.insertAvalancheImage(AvalancheImage(
              createTime = DateTime.now,
              avalanche = extId,
              filename = newFilename,
              origFilename = origFilename,
              mimeType = mimeType,
              size = rewrittenBytes.length,
              sortOrder = siblingImageCount
            )).map { _ =>
              dao.getAvalanche(extId).foreach { case avalanche if avalanche.viewable => s3.allowPublicImageAccess(extId) }
              ("extId" -> extId) ~ ("filename" -> newFilename) ~ ("origFilename" -> origFilename)
            }
          }
        }

      Future.sequence(jValueFutures.toList).map(jsonResponses => Ok(writeJson(jsonResponses)))
    }
  }

  def uploadScreenshot(extId: String) = UserAwareAction.async(parse.multipartFormData) { implicit request =>
    val uploadResult: Option[Future[Result]] = request.body.file(ScreenshotRequestFilename).map { formDataPart =>
      doScreenshotUpload(extId, request.user, formDataPart.ref.file)
    }
    uploadResult.getOrElse(Future { BadRequest })
  }

  private[controllers] def doScreenshotUpload(extId: String, user: Option[AvyEyesUser], screenshotFile: File): Future[Result] = {
    if (!isAuthorizedToEdit(extId, user, None)) {
      logger.warn(s"Not authorized to add screenshot to avalanche $extId")
      Future { Unauthorized }
    } else {
      val imageBytes = IOUtils.toByteArray(new FileInputStream(screenshotFile))
      logger.info(s"Uploading screenshot for new avalanche $extId in ${imageBytes.size} bytes")
      s3.uploadImage(extId, ScreenshotFilename, JpegMimeType, imageBytes).map(_ => Ok)
    }
  }

  def order(avyExtId: String, editKeyOpt: Option[String]) = UserAwareAction(parse.json) { implicit request =>
    if (!isAuthorizedToEdit(avyExtId, request.user, editKeyOpt)) {
      logger.warn(s"Not authorized to edit image order for avalanche $avyExtId")
      Unauthorized
    } else (request.body \ "order").as[JsArray] match {
      case JsArray(jsValues) =>
        dao.updateAvalancheImageOrder(avyExtId, jsValues.map(_.toString).toList)
        logger.debug(s"Successfully set image order on avalanche $avyExtId")
        Ok
      case _ =>
        logger.error("Received an image order PUT request, but the order payload was missing")
        BadRequest
    }
  }

  def caption(avyExtId: String, baseFilename: String, editKeyOpt: Option[String]) = UserAwareAction(parse.json) { implicit request =>
    if (!isAuthorizedToEdit(avyExtId, request.user, editKeyOpt)) {
      logger.warn(s"Not authorized to edit image caption for avalanche $avyExtId")
      Unauthorized
    } else (request.body \ "caption").as[JsString] match {
      case JsString(caption) if caption.nonEmpty =>
        dao.updateAvalancheImageCaption(avyExtId, baseFilename, Some(caption))
        logger.debug(s"Successfully set caption on $avyExtId/$baseFilename")
        Ok
      case JsString("") =>
        dao.updateAvalancheImageCaption(avyExtId, baseFilename, None)
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
    } else dao.getAvalancheImage(avyExtId, baseFilename).flatMap {
        case Some(image) =>
          s3.deleteImage(avyExtId, image.filename)
          dao.deleteAvalancheImage(avyExtId, image.filename)
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

}

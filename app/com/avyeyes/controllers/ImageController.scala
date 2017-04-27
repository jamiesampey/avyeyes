package com.avyeyes.controllers

import java.io.FileInputStream
import java.util.UUID
import javax.inject.Inject

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.{AvalancheImage, AvyEyesUser}
import com.avyeyes.service.{AmazonS3Service, ConfigurationService}
import com.avyeyes.system.UserEnvironment
import com.avyeyes.util.Constants.MaxImagesPerAvalanche
import com.sksamuel.scrimage.{Format, FormatDetector}
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.json4s.JsonDSL._
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
        val newFilename = s"${UUID.randomUUID().toString}.${imageFile.getName.split('.').last.toLowerCase}"

        val (mimeType, imageFormat) = FormatDetector.detect(imageBytes) match {
          case Some(Format.PNG) => ("image/png", Format.PNG)
          case Some(Format.GIF) => ("image/gif", Format.GIF)
          case _ => ("image/jpeg", Format.JPEG)
        }

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


}

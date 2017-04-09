//package com.avyeyes.rest
//
//import java.util.UUID
//
//import com.avyeyes.model.AvalancheImage
//import com.avyeyes.service.{Injectors, UnauthorizedException}
//import com.avyeyes.util.Constants._
//import com.avyeyes.util.FutureOps._
//import net.liftweb.common.Loggable
//import net.liftweb.http._
//import net.liftweb.http.rest.{RestContinuation, RestHelper}
//import net.liftweb.json.JsonAST._
//import net.liftweb.json.JsonDSL._
//import org.joda.time.DateTime
//
//import scala.concurrent.{ExecutionContext, Future}
//
//class Images extends RestHelper with Loggable {
//  val dal = Injectors.dal.vend
//  val s3 = Injectors.s3.vend
//  val R = Injectors.resources.vend
//  val user = Injectors.user.vend
//
//  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global
//
//  serve {
//    case "rest" :: "images" :: avyExtId :: "screenshot" :: Nil Post req => {
//      logger.info(s"Received screenshot for new avalanche $avyExtId")
//      val fph = req.uploadedFiles.head
//      s3.uploadImage(avyExtId, ScreenshotFilename, fph.mimeType, fph.file)
//      OkResponse()
//    }
//
//    case "rest" :: "images" :: avyExtId :: Nil Post req => RestContinuation.async { reply => {
//      tryImageUpload(avyExtId, req.uploadedFiles.head).map(response => reply(response))
//    }}
//
//    case "rest" :: "images" :: avyExtId :: baseFilename :: Nil JsonPut json->req =>
//      if (!user.isAuthorizedToEditAvalanche(avyExtId, S.param(EditParam))) {
//        logger.warn(s"Not authorized to edit image caption for avalanche $avyExtId")
//        UnauthorizedResponse("Not authorized to edit image caption")
//      } else json \ "caption" match {
//        case JString(caption) if caption.nonEmpty =>
//          dal.updateAvalancheImageCaption(avyExtId, baseFilename, Some(caption))
//          logger.debug(s"Successfully set caption on $avyExtId/$baseFilename")
//          OkResponse()
//        case JString("") =>
//          dal.updateAvalancheImageCaption(avyExtId, baseFilename, None)
//          logger.debug(s"Successfully cleared caption on $avyExtId/$baseFilename")
//          OkResponse()
//        case _ =>
//          logger.error("Received an image caption PUT request, but the caption payload was missing")
//          BadResponse()
//      }
//
//    case "rest" :: "images" :: avyExtId :: Nil JsonPut json->req =>
//      if (!user.isAuthorizedToEditAvalanche(avyExtId, S.param(EditParam))) {
//        logger.warn(s"Not authorized to edit image order for avalanche $avyExtId")
//        UnauthorizedResponse("Not authorized to edit image order")
//      } else json \ "order" match {
//        case JArray(order) =>
//          dal.updateAvalancheImageOrder(avyExtId, order.map(_.extract[String]))
//          logger.debug(s"Successfully set image order on avalanche $avyExtId")
//          OkResponse()
//        case _ =>
//          logger.error("Received an image order PUT request, but the order payload was missing")
//          BadResponse()
//      }
//
//    case "rest" :: "images" :: avyExtId :: baseFilename :: Nil Delete req =>
//      if (!user.isAuthorizedToEditAvalanche(avyExtId, S.param(EditParam))) {
//        logger.warn(s"Not authorized to delete image for avalanche $avyExtId")
//        UnauthorizedResponse("Not authorized to delete image")
//      } else {
//        dal.getAvalancheImage(avyExtId, baseFilename).flatMap {
//          case Some(image) =>
//            s3.deleteImage(avyExtId, image.filename)
//            dal.deleteAvalancheImage(avyExtId, image.filename)
//          case None => Future.failed(new RuntimeException("Couldn't find image"))
//        }.map { _ =>
//          logger.debug(s"Successfully deleted image $avyExtId/$baseFilename")
//          OkResponse()
//        }.recover {
//          case ue: UnauthorizedException => UnauthorizedResponse("AvyEyes auth required")
//          case ex: Exception =>
//            logger.error(s"Error while attempting to delete image $avyExtId/$baseFilename", ex)
//            InternalServerErrorResponse()
//        }.resolve
//      }
//
//  }
//
//  private[rest] def tryImageUpload(avyExtId: String, fph: FileParamHolder): Future[LiftResponse] = dal.countAvalancheImages(avyExtId).resolve match {
//    case _ if !user.isAuthorizedToEditAvalanche(avyExtId, S.param(EditParam)) =>
//      logger.warn(s"Not authorized to add images to avalanche $avyExtId")
//      Future { UnauthorizedResponse("Not authorized to add images to avalanche") }
//    case siblingImageCount if siblingImageCount >= MaxImagesPerAvalanche =>
//      logger.warn(s"Cannot add more images to avalanche $avyExtId")
//      Future { BadResponse() }
//    case siblingImageCount =>
//      val newFilename = s"${UUID.randomUUID().toString}.${fph.fileName.split('.').last.toLowerCase}"
//
//      s3.uploadImage(avyExtId, newFilename, fph.mimeType, fph.file).flatMap { _ =>
//        dal.insertAvalancheImage(AvalancheImage(
//          createTime = DateTime.now,
//          avalanche = avyExtId,
//          filename = newFilename,
//          origFilename = fph.fileName,
//          mimeType = fph.mimeType,
//          size = fph.length.toInt,
//          sortOrder = siblingImageCount
//        )).map { _ =>
//          dal.getAvalanche(avyExtId).foreach { case avalanche if avalanche.viewable => s3.allowPublicImageAccess(avyExtId) }
//          JsonResponse(("extId" -> avyExtId) ~ ("filename" -> newFilename) ~ ("origFilename" -> fph.fileName) ~ ("size" -> fph.length))
//        }
//      }
//  }
//
//}
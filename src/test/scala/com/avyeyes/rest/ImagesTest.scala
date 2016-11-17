package com.avyeyes.rest

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.AvalancheImage
import com.avyeyes.service.{UserSession, Injectors, AmazonS3ImageService, ResourceService}
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import com.avyeyes.test.LiftHelpers._
import com.avyeyes.util.Constants._
import net.liftweb.http._
import net.liftweb.json.JsonDSL._
import net.liftweb.mocks.MockHttpServletRequest
import net.liftweb.common.Box
import org.mockito.Matchers
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

import scala.concurrent.Future

class ImagesTest extends WebSpec2 with AroundExample with Mockito {
  sequential

  val mockUser = mock[UserSession]
  val mockResources = mock[ResourceService]
  val mockAvalancheDal = mock[CachedDAL]
  val mockS3 = mock[AmazonS3ImageService]

  mockResources.getProperty("s3.imageBucket") returns "some-bucket"
  mockResources.getProperty("s3.fullaccess.accessKeyId") returns "3490griow"
  mockResources.getProperty("s3.fullaccess.secretAccessKey") returns "34ijgeij4"

  def around[T: AsResult](t: => T): Result =
    Injectors.user.doWith(mockUser) {
      Injectors.resources.doWith(mockResources) {
        Injectors.dal.doWith(mockAvalancheDal) {
          Injectors.s3.doWith(mockS3) {
            AsResult(t)
          }
        }
      }
    }

  val extId = "4jf93dkj"
  val goodImgFileName = "imgInDb"
  val goodImgMimeType = "image/jpeg"
  val avalancheImage = avalancheImageForTest.copy(avalanche = extId, filename = goodImgFileName, mimeType = goodImgMimeType)
  
  val badImgFileName = "imgNotInDb"

  mockAvalancheDal.getAvalanche(extId) returns None
  mockAvalancheDal.getAvalancheImage(extId, goodImgFileName) returns Future.successful(Some(avalancheImage))
  mockAvalancheDal.getAvalancheImage(extId, badImgFileName) returns Future.successful(None)

  "Image post request" >> {
    isolated

    val mockPostRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId")
    mockPostRequest.method = "POST"

    val filename = "testImgABC.jpg"
    val fileBytes = Array[Byte](10, 20, 30, 40, 50)
    val fph = FileParamHolder("Test Image", "image/jpeg", filename, fileBytes)

    "Insert a new image in the DB" withSFor mockPostRequest in {
      val images = new Images
      mockAvalancheDal.countAvalancheImages(any[String]) returns Future.successful(0)
      mockAvalancheDal.insertAvalancheImage(any[AvalancheImage]) returns Future.successful(1)

      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns true

      val req = openLiftReqBox(S.request)
      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(images(reqWithFPH)())
        
      there was one(mockAvalancheDal).insertAvalancheImage(any[AvalancheImage])
      resp must beAnInstanceOf[JsonResponse]
      extractJsonStringField(resp, "extId") mustEqual extId
      extractJsonStringField(resp, "filename").length must beGreaterThan(0)
      extractJsonStringField(resp, "origFilename") mustEqual filename
      extractJsonLongField(resp, "size") mustEqual fileBytes.length
    }

    "Don't insert an image above the max images count" withSFor mockPostRequest in {
      val images = new Images
      mockAvalancheDal.countAvalancheImages(any[String]) returns Future.successful(MaxImagesPerAvalanche)
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns true

      val req = openLiftReqBox(S.request)
      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(images(reqWithFPH)())

      there was no(mockAvalancheDal).insertAvalancheImage(any[AvalancheImage])
      resp must beAnInstanceOf[BadResponse]
    }

    "Don't insert an image if the user is not authorized" withSFor mockPostRequest in {
      val images = new Images
      mockAvalancheDal.countAvalancheImages(any[String]) returns Future.successful(0)
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns false

      val req = openLiftReqBox(S.request)
      val reqWithFPH = addFileUploadToReq(req, fph)
      val resp = openLiftRespBox(images(reqWithFPH)())

      there was no(mockAvalancheDal).insertAvalancheImage(any[AvalancheImage])
      resp must beAnInstanceOf[UnauthorizedResponse]
    }
  }

  "Image caption put request" >> {
    val mockCaptionPutRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId/$goodImgFileName")
    mockCaptionPutRequest.method = "PUT"
    mockCaptionPutRequest.contentType = "application/json"

    "Write a caption to the DB" >> {
      val testCaption = "look at this crazy avalanche"
      mockCaptionPutRequest.body_=("caption" -> testCaption)

      "if the user is authorized" withSFor mockCaptionPutRequest in {
        mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns true

        val extIdArg = capture[String]
        val filenameArg = capture[String]
        val captionArg = capture[Option[String]]

        val images = new Images
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(images(req)())

        resp must beAnInstanceOf[OkResponse]
        there was one(mockAvalancheDal).updateAvalancheImageCaption(extIdArg, filenameArg, captionArg)
        extIdArg.value mustEqual extId
        filenameArg.value mustEqual goodImgFileName
        captionArg.value mustEqual Some(testCaption)
      }

      "if the user is not authorized" withSFor mockCaptionPutRequest in {
        mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns false

        val images = new Images
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(images(req)())

        there was no(mockAvalancheDal).updateAvalancheImageCaption(any, any, any)
        resp must beAnInstanceOf[UnauthorizedResponse]
      }
    }

    "Remove a caption from the DB" >> {
      mockCaptionPutRequest.body_=("caption" -> "")

      "do request" withSFor mockCaptionPutRequest in {
        mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns true

        val extIdArg = capture[String]
        val filenameArg = capture[String]
        val captionArg = capture[Option[String]]

        val images = new Images
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(images(req)())

        resp must beAnInstanceOf[OkResponse]
        there was one(mockAvalancheDal).updateAvalancheImageCaption(extIdArg, filenameArg, captionArg)
        extIdArg.value mustEqual extId
        filenameArg.value mustEqual goodImgFileName
        captionArg.value mustEqual None
      }
    }
  }

  "Image Order put request" >> {
    val mockOrderPutRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId")
    mockOrderPutRequest.method = "PUT"
    mockOrderPutRequest.contentType = "application/json"

    "Write image order to the DB" >> {
      val testImageOrder = List(
        "a40ee710-645f-43ce-9130-3d7dfd773c55",
        "5d4e3a37-7fb7-4d73-8276-8f550b09f8ae",
        "449162f5-1a75-4b3f-ab13-8903ae3bdce6",
        "cdc9761c-15ba-4be8-99f2-e4bb3d81eb60"
      )
      mockOrderPutRequest.body_=("order" -> testImageOrder)

      "if the user is authorized" withSFor mockOrderPutRequest in {
        mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns true

        val extIdArg = capture[String]
        val orderArg = capture[List[String]]

        val images = new Images
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(images(req)())

        resp must beAnInstanceOf[OkResponse]
        there was one(mockAvalancheDal).updateAvalancheImageOrder(extIdArg, orderArg)
        extIdArg.value mustEqual extId
        orderArg.value mustEqual testImageOrder
      }

      "if the user is not authorized" withSFor mockOrderPutRequest in {
        mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns false

        val images = new Images
        val req = openLiftReqBox(S.request)
        val resp = openLiftRespBox(images(req)())

        there was no(mockAvalancheDal).updateAvalancheImageOrder(any, any)
        resp must beAnInstanceOf[UnauthorizedResponse]
      }
    }
  }

  "Image Delete request" >> {
    val mockDeleteRequest = new MockHttpServletRequest(s"http://avyeyes.com/rest/images/$extId/$goodImgFileName")
    mockDeleteRequest.method = "DELETE"

    "if the user is authorized" withSFor mockDeleteRequest in {
      mockAvalancheDal.deleteAvalancheImage(extId, goodImgFileName) returns Future.successful({})
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns true

      val s3ExtIdArg = capture[String]
      val s3FilenameArg = capture[String]
      val dalExtIdArg = capture[String]
      val dalFilenameArg = capture[String]

      val images = new Images
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(images(req)())

      there was one(mockS3).deleteImage(s3ExtIdArg, s3FilenameArg)
      there was one(mockAvalancheDal).deleteAvalancheImage(dalExtIdArg, dalFilenameArg)
      resp must beAnInstanceOf[OkResponse]
      s3ExtIdArg.value mustEqual extId
      s3FilenameArg.value mustEqual goodImgFileName
      dalExtIdArg.value mustEqual extId
      dalFilenameArg.value mustEqual goodImgFileName
    }

    "if the user is not authorized" withSFor mockDeleteRequest in {
      mockUser.isAuthorizedToEditAvalanche(Matchers.eq(extId), any[Box[String]]) returns false

      val images = new Images
      val req = openLiftReqBox(S.request)
      val resp = openLiftRespBox(images(req)())

      there was no(mockS3).deleteImage(any, any)
      there was no(mockAvalancheDal).deleteAvalancheImage(any, any)
      resp must beAnInstanceOf[UnauthorizedResponse]
    }
  }
}

package com.jamiesampey.avyeyes.data

import play.api.test.WithApplication

import scala.concurrent.Future

class AvalancheDaoImageTest extends DatabaseTest {

  implicit val subject = injector.instanceOf[AvalancheDao]

  val testAvalanche = genAvalanche.generate.copy(extId = "5j3fyjd9", viewable = false)

  "Avalanche Images" should {
    val nonExistentAvalancheExtId = "594jk3i3"

    val img1 = genAvalancheImage.generate.copy(avalanche = testAvalanche.extId)
    val img2 = genAvalancheImage.generate.copy(avalanche = nonExistentAvalancheExtId)

    "Image insert and select works" in new WithApplication(appBuilder.build) {
      subject.insertAvalanche(testAvalanche).flatMap(_ => subject.insertAvalancheImage(img1)).resolve

      val returnedImage = subject.getAvalancheImage(testAvalanche.extId, img1.filename).resolve.get

      returnedImage.avalanche mustEqual testAvalanche.extId
      returnedImage.filename mustEqual img1.filename
      returnedImage.mimeType mustEqual img1.mimeType
      returnedImage.caption mustEqual img1.caption
    }

    "Images select by avy extId only works" in new WithApplication(appBuilder.build) {
      subject.insertAvalanche(testAvalanche).resolve
      subject.insertAvalancheImage(img1).resolve
      subject.insertAvalancheImage(img2).resolve

      val returnedImages = subject.getAvalancheImages(testAvalanche.extId).resolve
      returnedImages must haveLength(1)
      returnedImages.head mustEqual img1
    }

    "Image caption update works" in new WithApplication(appBuilder.build) {
      subject.insertAvalanche(testAvalanche).flatMap( _ => subject.insertAvalancheImage(img1)).resolve

      val testCaption = "look at this image!"
      subject.updateAvalancheImageCaption(testAvalanche.extId, img1.filename, Some(testCaption))
      val imageOpt = subject.getAvalancheImage(testAvalanche.extId, img1.filename).resolve

      imageOpt.get.caption mustEqual Some(testCaption)
    }

    "Image caption remove works" in new WithApplication(appBuilder.build) {
      subject.insertAvalanche(testAvalanche).flatMap( _ => subject.insertAvalancheImage(img1.copy(caption = Some("some caption text")))).resolve

      subject.updateAvalancheImageCaption(testAvalanche.extId, img1.filename, None).resolve
      val imageOpt = subject.getAvalancheImage(testAvalanche.extId, img1.filename).resolve

      imageOpt.get.caption must beNone
    }

    "Image order works" in new WithApplication(appBuilder.build) {
      val order0 = genAvalancheImage.generate.copy(avalanche = testAvalanche.extId, sortOrder = 0)
      val order1 = genAvalancheImage.generate.copy(avalanche = testAvalanche.extId, sortOrder = 1)
      val order2 = genAvalancheImage.generate.copy(avalanche = testAvalanche.extId, sortOrder = 2)

      Future.sequence(List(
        subject.insertAvalancheImage(order2),
        subject.insertAvalancheImage(order0),
        subject.insertAvalancheImage(order1)
      )).resolve

      val firstImageList = subject.getAvalancheImages(testAvalanche.extId).resolve.map(_.filename)
      subject.updateAvalancheImageOrder(testAvalanche.extId, List(order1.filename, order2.filename, order0.filename)).resolve
      val secondImageList = subject.getAvalancheImages(testAvalanche.extId).resolve.map(_.filename)

      firstImageList mustEqual List(order0.filename, order1.filename, order2.filename)
      secondImageList mustEqual List(order1.filename, order2.filename, order0.filename)
    }

    "Image delete works" in new WithApplication(appBuilder.build) {
      subject.insertAvalanche(testAvalanche).flatMap( _ => subject.insertAvalancheImage(img1)).resolve

      subject.getAvalancheImage(testAvalanche.extId, img1.filename).resolve must beSome
      subject.deleteAvalancheImage(testAvalanche.extId, img1.filename)
      subject.getAvalancheImage(testAvalanche.extId, img1.filename).resolve must beNone
    }

    "Image count works" in new WithApplication(appBuilder.build) {
      Future.sequence(List(subject.insertAvalancheImage(img1), subject.insertAvalancheImage(img2))).resolve
      subject.insertAvalancheImage(genAvalancheImage.generate.copy(avalanche = img1.avalanche)).resolve
      subject.countAvalancheImages(img1.avalanche).resolve mustEqual 2
      subject.countAvalancheImages(img2.avalanche).resolve mustEqual 1
    }

  }
}

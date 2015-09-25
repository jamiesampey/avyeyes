package com.avyeyes.test

import java.util.UUID

import com.avyeyes.model._
import com.avyeyes.model.enums.{ExperienceLevel, _}
import com.avyeyes.util.Constants._
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime
import org.scalacheck.Gen

object Generators {

  def genDateTime(minMillis: Long = 0): Gen[DateTime] = Gen.choose(minMillis, DateTime.now.getMillis).map(new DateTime(_))

  def genCoordinate = for {
    lng <- Gen.choose(-180.0, 180.0)
    lat <- Gen.choose(-90.0, 90.0)
    alt <- Gen.choose(0, 8000)
  } yield Coordinate(lng, lat, alt)

  def genScene = for {
    sky <- Gen.oneOf(SkyCoverage.values.toSeq)
    precip <- Gen.oneOf(Precipitation.values.toSeq)
  } yield Scene(sky, precip)

  def genSlope = for {
    aspect <- Gen.oneOf(Aspect.values.toSeq)
    angle <- Gen.choose(0, 90)
    elevation <- Gen.choose(0, 8000)
  } yield Slope(aspect, angle, elevation)

  def genClassification = for {
    avyType <- Gen.oneOf(AvalancheType.values.toSeq)
    trigger <- Gen.oneOf(AvalancheTrigger.values.toSeq)
    interface <- Gen.oneOf(AvalancheInterface.values.toSeq)
    rSize <- Gen.choose(0.0, 5.0)
    dSize <- Gen.choose(0.0, 5.0)
  } yield Classification(avyType, trigger, interface, rSize, dSize)

  def genHumanNumbers = for {
    modeOfTravel <- Gen.oneOf(ModeOfTravel.values.toSeq)
    caught <- Gen.choose(-1, 50)
    partiallyBuried <- Gen.choose(-1, 50)
    fullyBuried <- Gen.choose(-1, 50)
    injured <- Gen.choose(-1, 50)
    killed <- Gen.choose(-1, 50)
  } yield HumanNumbers(modeOfTravel, caught, partiallyBuried, fullyBuried, injured, killed)

  private def genAvalanche: Gen[Avalanche] = for {
      createTime <- genDateTime()
      updateTime <- genDateTime(createTime.getMillis)
      extId <- Gen.const(RandomStringUtils.random(ExtIdLength, ExtIdChars))
      viewable <- Gen.oneOf(true, false)
      submitterEmail <- Gen.alphaStr
      submitterExp <- Gen.oneOf(ExperienceLevel.values.toSeq)
      location <- genCoordinate
      areaName <- Gen.alphaStr
      date <- genDateTime()
      scene <- genScene
      slope <- genSlope
      classification <- genClassification
      humanNumbers <- genHumanNumbers
      perimeter <- Gen.listOf(genCoordinate)
      comments <- Gen.option(Gen.alphaStr)
    } yield Avalanche(
      createTime = createTime,
      updateTime = updateTime,
      extId = extId,
      viewable = viewable,
      submitterEmail = submitterEmail,
      submitterExp = submitterExp,
      location = location,
      areaName = areaName,
      date = date,
      scene = scene,
      slope = slope,
      classification = classification,
      humanNumbers = humanNumbers,
      perimeter = perimeter,
      comments = comments
    )

  def avalancheForTest = genAvalanche.sample.get

  def genAvalancheImage = for {
    createTime <- genDateTime()
    avyExtId <- Gen.const(RandomStringUtils.random(ExtIdLength, ExtIdChars))
    filename <- Gen.const(s"${UUID.randomUUID().toString}.jpg")
    origFilename <- Gen.alphaStr
    mimeType <- Gen.alphaStr
    size <- Gen.choose(1000, MaxImageSize)
  } yield AvalancheImage(
    createTime = createTime,
    avyExtId = avyExtId,
    filename = filename,
    origFilename = origFilename,
    mimeType = mimeType,
    size = size
  )

  def avalancheImageForTest = genAvalancheImage.sample.get
}
package helpers

import java.util.UUID

import com.jamiesampey.avyeyes.model._
import com.jamiesampey.avyeyes.model.enums.{ExperienceLevel, _}
import com.jamiesampey.avyeyes.service.AvyEyesUserService._
import com.jamiesampey.avyeyes.util.Constants._
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime
import org.scalacheck.Gen

trait Generators {

  protected implicit class GenOps[T](gen: Gen[T]) {
    implicit def generate: T = gen.sample.getOrElse(gen.generate)
  }

  protected def genDateTime(minMillis: Long = 0): Gen[DateTime] = Gen.choose(minMillis, DateTime.now.getMillis).map(new DateTime(_))

  protected def genCoordinate = for {
    lng <- Gen.choose(-180.0, 180.0)
    lat <- Gen.choose(-90.0, 90.0)
    alt <- Gen.choose(0, 8000)
  } yield Coordinate(lng, lat, alt)

  protected def genWeather = for {
    snow <- Gen.choose(-1, 1000)
    windSpeed <- Gen.oneOf(WindSpeed.values.toSeq)
    windDirection <- Gen.oneOf(Direction.values.toSeq)
  } yield Weather(snow, windSpeed, windDirection)

  protected def genSlope = for {
    aspect <- Gen.oneOf(Direction.values.toSeq)
    angle <- Gen.choose(0, 90)
    elevation <- Gen.choose(0, 8000)
  } yield Slope(aspect, angle, elevation)

  protected def genClassification = for {
    avyType <- Gen.oneOf(AvalancheType.values.toSeq)
    trigger <- Gen.oneOf(AvalancheTrigger.values.toSeq)
    triggerModifier <- Gen.oneOf(AvalancheTriggerModifier.values.toSeq)
    interface <- Gen.oneOf(AvalancheInterface.values.toSeq)
    rSize <- Gen.choose(0, 5)
    dSize <- Gen.oneOf(0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)
  } yield Classification(avyType, trigger, triggerModifier, interface, rSize, dSize)

  protected def genHumanNumbers = for {
    modeOfTravel <- Gen.oneOf(ModeOfTravel.values.toSeq)
    caught <- Gen.choose(-1, 50)
    partiallyBuried <- Gen.choose(-1, 50)
    fullyBuried <- Gen.choose(-1, 50)
    injured <- Gen.choose(-1, 50)
    killed <- Gen.choose(-1, 50)
  } yield HumanNumbers(modeOfTravel, caught, partiallyBuried, fullyBuried, injured, killed)

  protected def genAvalanche: Gen[Avalanche] = for {
      createTime <- genDateTime()
      updateTime <- genDateTime(createTime.getMillis)
      extId <- Gen.const(RandomStringUtils.random(ExtIdLength, ExtIdChars))
      viewable <- Gen.oneOf(true, false)
      submitterEmail <- Gen.alphaStr
      submitterExp <- Gen.oneOf(ExperienceLevel.values.toSeq)
      location <- genCoordinate
      areaName <- Gen.alphaStr
      date <- genDateTime()
      weather <- genWeather
      slope <- genSlope
      classification <- genClassification
      humanNumbers <- genHumanNumbers
      perimeter <- Gen.nonEmptyListOf(genCoordinate)
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
      weather = weather,
      slope = slope.copy(elevation = location.altitude.toInt),
      classification = classification,
      humanNumbers = humanNumbers,
      perimeter = perimeter,
      comments = comments
    )

  protected def genAvalancheImage = for {
    createTime <- genDateTime()
    avalanche <- Gen.const(RandomStringUtils.random(ExtIdLength, ExtIdChars))
    filename <- Gen.const(s"${UUID.randomUUID().toString}.jpg")
    origFilename <- Gen.alphaStr
    mimeType <- Gen.alphaStr
    size <- Gen.choose(1000, 5000000)
    sort_order <- Gen.choose(0, 19)
    caption <- Gen.option(Gen.alphaStr)
  } yield AvalancheImage(
    createTime = createTime,
    avalanche = avalanche,
    filename = filename,
    origFilename = origFilename,
    mimeType = mimeType,
    size = size,
    sortOrder = sort_order,
    caption = caption
  )

  protected def genAvyEyesUser = for {
    createTime <- genDateTime()
    lastActivityTime <- genDateTime()
    email <- Gen.alphaStr
    facebookId <- Gen.option(Gen.alphaStr)
    passwordHash <- Gen.option(Gen.alphaStr)
    roles <- Gen.listOfN(2, Gen.oneOf(SiteOwnerRole, AdminRole))
  } yield AvyEyesUser(
    createTime = createTime,
    lastActivityTime = lastActivityTime,
    email = email,
    facebookId = facebookId,
    passwordHash = passwordHash,
    roles = roles
  )
}
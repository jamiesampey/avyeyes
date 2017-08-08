package com.jamiesampey.avyeyes.util

import scala.concurrent.duration._

object Constants {
	val CamAltitudePinThreshold: Int = 15000 // meters
	val CamRangePinThreshold = 5.0 // miles

  val MaxImagesPerAvalanche = 20

  val AvalancheEditWindow: Duration = 7.days

	val ExtIdLength = 8
	val ExtIdChars = "0123456789abcdefghijklmnopqrstuvwxyz"

  val ScreenshotFilename = "screenshot.jpg"
  val ScreenshotRequestFilename = "screenshot"
  val FacebookSharePageFilename = "facebook-share.html"
}
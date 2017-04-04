package com.avyeyes.util

import scala.concurrent.duration._

object Constants {
  val IndexPath = "index"
  val BrowserNotSupportedPath = "whawha"
  val LoginPath = "whodat"
  val EditParam = "edit"
  val ExtIdUrlParam = "extId"

  val ChromeMinVersion: Double = 35.0
  val FirefoxMinVersion: Double = 30.0
  val IeMinVersion: Double = 10.0
  val SafariMinVersion: Double = 7.0
    
	val HumanNumberUnknown: Int = -1

	val CamPitchCutoff: Double = -45.0
	val CamAltitudeLimit: Int = 10000
	val AvyDistRangeMiles = 5.0

  val MaxImagesPerAvalanche = 20

  val AvalancheEditWindow = 7 days

	val ExtIdLength = 8
	val ExtIdChars = "0123456789abcdefghijklmnopqrstuvwxyz"

  val SiteOwnerRole = "site_owner"
  val AdminRole = "admin"

  val ScreenshotFilename = "screenshot.jpg"
  val FacebookSharePageFilename = "facebook-share.html"
}
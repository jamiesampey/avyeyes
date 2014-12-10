package com.avyeyes.util

import java.util.Date

object Constants {
  val IndexPath = "index"
  val BrowserNotSupportedPath = "whawha"
  val LoginPath = "whodat"
    
  val ChromeVersion: Double = 35.0
  val FirefoxMinVersion: Double = 30.0
  val OperaMinVersion: Double = 22.0
  val SafariMinVersion: Double = 6.0
  val IeMinVersion: Double = 9.0
    
	val EarliestAvyDate = new Date(0) // start of Epoch time. Midnight on Jan 1, 1970 GMT
	val HumanNumberUnknown: Int = -1

	val CamTiltRangeCutoff = 45.0
	val CamRelAltLimitMeters = 8000.0
	val AvyDistRangeMiles = 10.0
	val EarthRadiusMiles = 3959.0
	
	val MaxImageSize = 10000000L
  val MaxImagesPerAvalanche = 10

	val ExtIdUrlParam = "extId"
	val ExtIdLength = 8
	val ExtIdChars = "0123456789abcdefghijklmnopqrstuvwxyz"
}
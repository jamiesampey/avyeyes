package com.avyeyes.util

import java.util.Date
import net.liftweb.util.Props

object AEConstants {
    val ChromeSupportedVersion: Double = 35.0
    val FirefoxSupportedVersion: Double = 30.0
    val SafariSupportedVersion: Int = 6
    val IeSupportedVersion: Int = 9
    
	val EarliestAvyDate = new Date(0) // start of Epoch time. Midnight on Jan 1, 1970 GMT
	val HumanNumberUnknown: Int = -1

	val CamTiltRangeCutoff = 45.0
	val CamRelAltLimitMeters = 9500.0
	val AvyDistRangeMiles = 10.0
	val EarthRadiusMiles = 3959.0
	
	val MaxImageSize = 30000000L

	val ExtIdUrlParam = "extId"
	val ExtIdLength = 8
	val ExtIdChars = "0123456789abcdefghijklmnopqrstuvwxyz"
}
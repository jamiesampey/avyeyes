package com.avyeyes.util

import java.util.Date
import net.liftweb.util.Props

object AEConstants {
    val JDBC_CONNECT_STR = new StringBuilder("jdbc:postgresql://")
        .append(Props.get("db.host").get).append(":")
        .append(Props.get("db.port").get).append("/")
        .append(Props.get("db.name").get).toString
    
    val ChromeSupportedVersion = 35.0
    val FirefoxSupportedVersion = 30.0
    val SafariSupportedVersion = 5.1
    val IeSupportedVersion = 9.0
    
	val earliestAvyDate = new Date(0) // start of Epoch time. Midnight on Jan 1, 1970 GMT
	val humanNumberUnknown: Int = -1

	val CAM_TILT_RANGE_CUTOFF = 45.0
	val CAM_REL_ALT_LIMIT_METERS = 9500.0
	val AVY_DIST_RANGE_MILES = 10.0
	val EARTH_RADIUS_MILES = 3959.0
	
	val MAX_IMAGE_SIZE = 30000000L

	val EXT_ID_URL_PARAM = "extId"
	val EXT_ID_LENGTH = 8
	val EXT_ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz"
}
package com.avyeyes.util

import java.util.Date

object AEConstants {
	def earliestAvyDate = new Date(0) // start of Epoch time. Midnight on Jan 1, 1970 GMT
	def humanNumberUnknown: Int = -1

	def CAM_TILT_RANGE_CUTOFF = 45.0
	def CAM_REL_ALT_LIMIT_METERS = 9500.0
	def AVY_DIST_RANGE_MILES = 10.0
	def EARTH_RADIUS_MILES = 3959.0
	


	def EXT_ID_URL_PARAM = "extId"
	def EXT_ID_LENGTH = 8
	def EXT_ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz"
}
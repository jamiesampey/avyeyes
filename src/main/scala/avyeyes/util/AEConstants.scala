package avyeyes.util

import java.util.Date

object AEConstants {
	def earliestAvyDate = new Date(0) // start of Epoch time. Midnight on Jan 1, 1970 GMT
	def humanNumberUnknown: Int = -1

	def CAM_TILT_RANGE_CUTOFF = 45.0
	def CAM_REL_ALT_LIMIT_METERS = 4500.0
	def AVY_DIST_RANGE_MILES = 10.0
	def EARTH_RADIUS_MILES = 3959.0
	
	def JQAC_LABEL = "label"
	def JQAC_VALUE = "value"
	def UNKNOWN_CODE = "U"
	def UNKNOWN_LABEL = "Unknown"
}
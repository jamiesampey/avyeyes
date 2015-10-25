package com.avyeyes.model

import com.avyeyes.model.enums.Aspect.Aspect
import com.avyeyes.model.StringSerializers.enumValueToCode

case class Slope(aspect: Aspect, angle: Int, elevation: Int) {
  override def toString = s"${enumValueToCode(aspect)}-$angle-$elevation"
}

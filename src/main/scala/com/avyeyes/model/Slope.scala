package com.avyeyes.model

import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect.Aspect
import com.avyeyes.model.StringSerializers.enumValueToCode

case class Slope(aspect: Aspect = Aspect.N,
                 angle: Int = 0,
                 elevation: Int = 0) {
  override def toString = s"${enumValueToCode(aspect)}-$angle-$elevation"
}

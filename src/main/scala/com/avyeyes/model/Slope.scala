package com.avyeyes.model

import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect.Aspect

case class Slope(aspect: Aspect = Aspect.N,
                 angle: Int = 0,
                 elevation: Int = 0)

object Slope {
  implicit def toString(s: Slope) = s"${s.aspect.toString}-${s.angle}-${s.elevation}"

  implicit def fromString(str: String) = {
    val arr = str.split("-")
    Slope(
      aspect = Aspect.withName(arr(0)),
      angle = arr(1).toInt,
      elevation = arr(2).toInt
    )
  }
}

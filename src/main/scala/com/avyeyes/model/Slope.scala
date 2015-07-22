package com.avyeyes.model

import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect.Aspect

case class Slope(aspect: Aspect = Aspect.N,
                 angle: Int = 0,
                 elevation: Int = 0) {
  override def toString = s"${aspect.toString}-$angle-$elevation"
}

object Slope {
  def fromString(str: String) = {
    val arr = str.split("-")
    Slope(
      aspect = Aspect.withName(arr(0)),
      angle = arr(1).toInt,
      elevation = arr(2).toInt
    )
  }
}

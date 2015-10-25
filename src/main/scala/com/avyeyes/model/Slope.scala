package com.avyeyes.model

import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect.Aspect

case class Slope(aspect: Aspect, angle: Int, elevation: Int) {
  override def toString = s"${Aspect.toCode(aspect)}-$angle-$elevation"
}

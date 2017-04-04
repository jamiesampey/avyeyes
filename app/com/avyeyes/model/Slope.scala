package com.avyeyes.model

import com.avyeyes.model.enums.Direction
import com.avyeyes.model.enums.Direction.Direction

case class Slope(aspect: Direction, angle: Int, elevation: Int) {
  override def toString = s"${Direction.toCode(aspect)}-$angle-$elevation"
}

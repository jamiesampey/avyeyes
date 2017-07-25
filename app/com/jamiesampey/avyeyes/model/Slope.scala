package com.jamiesampey.avyeyes.model

import com.jamiesampey.avyeyes.model.enums.Direction
import com.jamiesampey.avyeyes.model.enums.Direction.Direction

case class Slope(aspect: Direction, angle: Int, elevation: Int) {
  override def toString = s"${Direction.toCode(aspect)}-$angle-$elevation"
}

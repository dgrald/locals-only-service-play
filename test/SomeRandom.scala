import services.PointLocation

import scala.util.Random

/**
  * Created by dylangrald on 11/13/16.
  */
object SomeRandom {

  val random = new Random()

  def double(): Double = {
    random.nextDouble()
  }

  def pointLocation(): PointLocation = {
    PointLocation(double(), double())
  }
}

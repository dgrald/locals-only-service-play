import services.{Location, Stash, PointLocation}

import scala.util.Random

/**
  * Created by dylangrald on 11/13/16.
  */
object SomeRandom {

  val random = new Random()

  def double(): Double = {
    random.nextDouble()
  }

  def string(): String = {
    random.alphanumeric.take(10).mkString("")
  }

  def pointLocation(): PointLocation = {
    PointLocation(double(), double())
  }

  def pointLocationStash(): Stash = {
    pointLocationStash(pointLocation())
  }

  def pointLocationStash(location: Location): Stash = {
    Stash(string(), location)
  }
}

import services._

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
    stash(pointLocation())
  }

  def stash(location: Location): Stash = {
    Stash(string(), location)
  }

  def lineLocation(): LineLocation = {
    LineLocation(List(doublePair(), doublePair(), doublePair()))
  }

  def lineLocationStash(): Stash = {
    stash(lineLocation())
  }

  def polygonLocation(): PolygonLocation = {
    PolygonLocation(List(doublePair(), doublePair(), doublePair()))
  }

  def polygonLocationStash(): Stash = {
    stash(polygonLocation())
  }

  def doublePair(): (Double, Double) = (double(), double())
}

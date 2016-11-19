import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.play.json.JSONSerializationPack
import services.StashStore

import scala.concurrent.Future

/**
  * Created by dylangrald on 11/4/16.
  */
class StashStoreSpec extends PlaySpec with ScalaFutures {

  val reactiveMongoApi = new ReactiveMongoApi {
    override def driver: MongoDriver = new MongoDriver()

    override def gridFS: GridFS[JSONSerializationPack.type] = ???

    @deprecated("Use [[database]]")
    override def db: DefaultDB = ???

    override def connection: MongoConnection = driver.connection(List("localhost"))

    override def database: Future[DefaultDB] = connection.database("test-collection")

    override def asyncGridFS: Future[GridFS[JSONSerializationPack.type]] = ???
  }

  val stashStore = new StashStore(reactiveMongoApi)

  val patienceConfiguration = PatienceConfiguration.Timeout(Span(5, Seconds))

  "StashStore.addStash" should {
    "add an input PointLocation stash correctly" in {
      val inputPointStash = SomeRandom.pointLocationStash()

      val savedStash = stashStore.addStash(inputPointStash)

      savedStash.futureValue(patienceConfiguration) mustEqual inputPointStash
    }

    "add an input LineLocation stash correctly" in {
      val inputLineStash = SomeRandom.lineLocationStash()

      val savedStash = stashStore.addStash(inputLineStash)

      savedStash.futureValue(patienceConfiguration) mustEqual inputLineStash
    }

    "add an input PolygonLocation stash correctly" in {
      val inputPolygonStash = SomeRandom.polygonLocationStash()

      val savedStash = stashStore.addStash(inputPolygonStash)

      savedStash.futureValue(patienceConfiguration) mustEqual inputPolygonStash
    }
  }

  "StashStore.getStashes" should {
    "return stashes that were added" in {
      val inputPointStash = SomeRandom.pointLocationStash()
      val inputLineStash = SomeRandom.lineLocationStash()
      val inputPolygonStash = SomeRandom.polygonLocationStash()

      val allNewStashes = List(inputPointStash, inputLineStash, inputPolygonStash)

      val savedStash1 = stashStore.addStash(inputPointStash)
      whenReady(savedStash1) { saved1 =>
        val savedStash2 = stashStore.addStash(inputLineStash)
        whenReady(savedStash2) { saved2 =>
          val savedStash3 = stashStore.addStash(inputPolygonStash)
          whenReady(savedStash3) { saved3 =>
            val allStashes = stashStore.getStashes()
            allStashes.futureValue(patienceConfiguration).intersect(allNewStashes) mustEqual allNewStashes
          }
        }
      }
    }
  }
}

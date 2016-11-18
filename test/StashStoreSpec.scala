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

  val reactiveMongoApi = new ReactiveMongoApi {override def driver: MongoDriver = new MongoDriver()

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
        val inputPointStash = SomeRandom.pointLocation()

        val savedStash = stashStore.addStash(inputPointStash)

        savedStash.futureValue(patienceConfiguration) mustEqual inputPointStash
    }
  }

  "StashStore.getStashes" should {
    "return stashes that were added" in {
      val inputPointStash1 = SomeRandom.pointLocation()
      val inputPointStash2 = SomeRandom.pointLocation()
      val savedStash1 = stashStore.addStash(inputPointStash1)
      whenReady(savedStash1) { saved1 =>
        val savedStash2 = stashStore.addStash(inputPointStash2)
        whenReady(savedStash2) { saved2 =>
          val allStashes = stashStore.getStashes()
          allStashes.futureValue(patienceConfiguration).contains(inputPointStash1) mustBe true
          allStashes.futureValue(patienceConfiguration).contains(inputPointStash2) mustBe true
        }
      }
    }
  }
}

import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.play.json.JSONSerializationPack
import services.{Stash, StashStore}

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

  "StashController.getStash(id)" should {
    "return stash when it exists" in {
      val newStash = SomeRandom.lineLocationStash()

      val savedStashFuture = stashStore.addStash(newStash)
      whenReady(savedStashFuture) { saved =>
        val retrievedStashFuture = stashStore.getStash(newStash._id)
        retrievedStashFuture.futureValue(patienceConfiguration).get mustEqual newStash
      }
    }

    "return None when it does not exist" in {
      val id = SomeRandom.uuidString()

      val getStashFuture = stashStore.getStash(id)

      getStashFuture.futureValue(patienceConfiguration) mustEqual None
    }
  }

  "StashStore.deleteStash" should {
    "delete the stash with the specified id" in {
      val stash = SomeRandom.stash(SomeRandom.lineLocation())
      val addStashFuture = stashStore.addStash(stash)

      whenReady(addStashFuture) { addedStash =>
        val deleteStashFuture = stashStore.deleteStash(addedStash._id)
        whenReady(deleteStashFuture) { deletedStashResult =>
          val getAllStashesFuture = stashStore.getStashes()
          getAllStashesFuture.futureValue(patienceConfiguration).contains(stash) mustEqual false
        }
      }
    }
  }

  "StashStore.updateStash" should {
    "update the stash with the new fields" in {
      val stashId = SomeRandom.uuidString()
      val originalStash = Stash(stashId, SomeRandom.string(), SomeRandom.lineLocation())
      val updatedStashName = SomeRandom.string()
      val updatedStashLocation = SomeRandom.pointLocation()
      val updatedStash = Stash(stashId, updatedStashName, updatedStashLocation)

      val savedOriginalStashFuture = stashStore.addStash(originalStash)
      whenReady(savedOriginalStashFuture) { addedStash =>
        val updatedStashFuture = stashStore.updateStash(updatedStash)
        updatedStashFuture.futureValue(patienceConfiguration) mustEqual updatedStash

        val allStashesFuture = stashStore.getStashes()
        whenReady(allStashesFuture) { allStashes =>
          allStashes.find(s => s._id.equals(stashId)).get mustEqual updatedStash
        }
      }
    }
  }
}

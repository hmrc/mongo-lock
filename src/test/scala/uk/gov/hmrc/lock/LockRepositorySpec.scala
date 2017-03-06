/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.lock

import java.util.concurrent.TimeUnit

import org.joda.time.{DateTime, Duration}
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.enablers.Emptiness
import reactivemongo.api.commands.DefaultWriteResult
import uk.gov.hmrc.lock.LockFormats.Lock
import uk.gov.hmrc.mongo.{Awaiting, MongoSpecSupport, ReactiveRepository}
import uk.gov.hmrc.time.DateTimeUtils

import scala.concurrent.duration.FiniteDuration


class LockRepositorySpec extends WordSpecLike with Matchers with MongoSpecSupport with Awaiting with BeforeAndAfterEach with OptionValues with ScalaFutures with IntegrationPatience {
  import scala.concurrent.Future

  private implicit val now = DateTimeUtils.now
  val lockId = "testLock"
  val owner = "repoSpec"
  val testContext = this

  implicit def liftFuture[A](v: A) = Future.successful(v)

  implicit val reactiveRepositoryEmptiness = new Emptiness[ReactiveRepository[_, _]] {
    override def isEmpty(thing: ReactiveRepository[_, _]) = await(thing.count) == 0
  }

  val repo = new LockRepository {
    val retryIntervalMillis = FiniteDuration(5L, TimeUnit.SECONDS).toMillis

    override def withCurrentTime[A](f: (DateTime) => A) = f(now)
  }

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  def manuallyInsertLock(lock: Lock) = {
    await(repo.insert(lock))
  }

  "The lock method" should {

    "successfully create a lock if one does not already exist" in {
      await(repo.lock(lockId, owner, new Duration(1000L))) shouldBe true

      val lock = await(repo.findAll())

      lock.head shouldBe Lock(lockId, owner, now, now.plusSeconds(1))
    }

    "successfully create a lock if a different one already exists" in {
      manuallyInsertLock(Lock("nonMatchingLock", owner, now, now.plusSeconds(1)))

      await(repo.lock(lockId, owner, new Duration(1000L))) shouldBe true
      await(repo.count) shouldBe 2

      await(repo.findById(lockId)) shouldBe Some(Lock(lockId, owner, now, now.plusSeconds(1)))
    }

    "do not change a non-expired lock with a different owner" in {
      val alternativeOwner = "owner2"
      manuallyInsertLock(Lock(lockId, alternativeOwner, now, now.plusSeconds(100)))

      await(repo.lock(lockId, owner, new Duration(1000L))) shouldBe false
      await(repo.findById(lockId)).map(_.owner) shouldBe Some(alternativeOwner)
    }

    "do not change a non-expired lock with the same owner" in {

      await(repo.removeAll())

      val existingLock = Lock(lockId, owner, now.minusDays(1), now.plusDays(1))

      manuallyInsertLock(existingLock)

      await(repo.lock(lockId, owner, new Duration(1000L))) shouldBe false

      await(repo.findAll()).head shouldBe existingLock

    }

    "change an expired lock" in {
      val expiredLock = Lock(lockId, owner, now.minusDays(2), now.minusDays(1))

      manuallyInsertLock(expiredLock)

      val gotLock = await(repo.lock(lockId, owner, new Duration(1000L)))

      gotLock shouldBe true
      await(repo.findAll()).head shouldBe Lock(lockId, owner, now, now.plusSeconds(1))
    }
  }

  "The renew method" should {
    "not renew a lock if one does not already exist" in {
      await(repo.renew(lockId, owner, new Duration(1000L))) shouldBe false
      await(repo.findAll()) shouldBe empty
    }

    "not renew a different lock if one exists" in {
      manuallyInsertLock(Lock("nonMatchingLock", owner, now, now.plusSeconds(1)))

      await(repo.renew(lockId, owner, new Duration(1000L))) shouldBe false
      await(repo.findAll()).head shouldBe Lock("nonMatchingLock", owner, now, now.plusSeconds(1))
    }

    "not change a non-expired lock with a different owner" in {
      val alternativeOwner = "owner2"
      manuallyInsertLock(Lock(lockId, alternativeOwner, now, now.plusSeconds(100)))

      await(repo.renew(lockId, owner, new Duration(1000L))) shouldBe false
      await(repo.findById(lockId)).map(_.owner) shouldBe Some(alternativeOwner)
    }

    "change a non-expired lock with the same owner" in {
      val existingLock = Lock(lockId, owner, now.minusDays(1), now.plusDays(1))

      manuallyInsertLock(existingLock)

      await(repo.renew(lockId, owner, new Duration(1000L))) shouldBe true

      await(repo.findAll()).head shouldBe Lock(lockId, owner, existingLock.timeCreated, now.plus(new Duration(1000L)))
    }

    "not renew an expired lock" in {
      val expiredLock = Lock(lockId, owner, now.minusDays(2), now.minusDays(1))

      manuallyInsertLock(expiredLock)

      val gotLock = await(repo.renew(lockId, owner, new Duration(1000L)))

      gotLock shouldBe false
      await(repo.findAll()).head shouldBe expiredLock
    }
  }

  "The releaseLock method" should {

    "remove an owned and expired lock" in {
      val lock = Lock(lockId, owner, now.minusDays(2), now.minusDays(1))
      manuallyInsertLock(lock)

      await(repo.releaseLock(lockId, owner))

      await(repo.count) shouldBe 0
    }

    "remove an owned and unexpired lock" in {
      val lock = Lock(lockId, owner, now.minusDays(1), now.plusDays(1))
      manuallyInsertLock(lock)

      await(repo.releaseLock(lockId, owner))

      await(repo.count) shouldBe 0
    }

    "do nothing if the lock doesn't exist" in {
      await(repo.releaseLock(lockId, owner))

      await(repo.count) shouldBe 0
    }

    "leave an expired lock owned by someone else" in {

      val someoneElsesExpiredLock = Lock(lockId, "someoneElse", now.minusDays(2), now.minusDays(1))
      manuallyInsertLock(someoneElsesExpiredLock)

      await(repo.releaseLock(lockId, owner))

      await(repo.findAll()).head shouldBe someoneElsesExpiredLock
    }

    "leave an unexpired lock owned by someone else" in {

      val someoneElsesLock = Lock(lockId, "someoneElse", now.minusDays(2), now.plusDays(1))
      manuallyInsertLock(someoneElsesLock)

      await(repo.releaseLock(lockId, owner))

      await(repo.findAll()).head shouldBe someoneElsesLock
    }

    "leave a different owned lock" in {

      val someOtherLock = Lock("someOtherLock", owner, now.minusDays(1), now.plusDays(1))
      manuallyInsertLock(someOtherLock)

      await(repo.releaseLock(lockId, owner))

      await(repo.findAll()).head shouldBe someOtherLock
    }
  }

  "The isLocked method" should {
    "return false if no lock obtained" in {
      await(repo.isLocked(lockId, owner)) should be (false)
    }

    "return true if lock held" in {
      manuallyInsertLock(Lock(lockId, owner, now, now.plusSeconds(100)))
      await(repo.isLocked(lockId, owner)) should be (true)
    }

    "return false if the lock is held but expired" in {
      manuallyInsertLock(Lock(lockId, owner, now.minusDays(2), now.minusDays(1)))
      await(repo.isLocked(lockId, owner)) should be (false)
    }
  }

  "The lock keeper" should {
    val lockKeeper = new LockKeeper {
      val forceLockReleaseAfter = Duration.standardSeconds(1)
      override lazy val serverId: String = testContext.owner
      val lockId: String = testContext.lockId
      val repo: LockRepository = testContext.repo
    }

    "run the block supplied if the lock can be obtained, and return an option on the result and release the lock" in {
      def hasLock = {
        await(repo.findById(lockId)) shouldBe Some(Lock(lockId, owner, now, now.plusSeconds(1)))
        Future.successful("testString")
      }

      await(lockKeeper.tryLock[String](hasLock)) shouldBe Some("testString")
      repo shouldBe empty
    }

    "run the block supplied and release the lock even if the block returns a failed future" in {
      a [RuntimeException] should be thrownBy{
        await(lockKeeper.tryLock(Future.failed(new RuntimeException)))
      }
      repo shouldBe empty
    }

    "run the block supplied and release the lock even if the block throws an exception" in {
      a [RuntimeException] should be thrownBy{
        await(lockKeeper.tryLock(throw new RuntimeException ))
      }
      repo shouldBe empty
    }

    "not run the block supplied if the lock is owned by someone else, and return None" in {
      val manualyInsertedLock = Lock(lockId, "owner2", now, now.plusSeconds(100))
      manuallyInsertLock(manualyInsertedLock)

      await(lockKeeper.tryLock {
        fail("Should not be run!")
      }) shouldBe None

      await(repo.findAll()).head shouldBe manualyInsertedLock
    }

    "not run the block supplied if the lock is already owned by the caller, and return None" in {
      val manualyInsertedLock = Lock(lockId, owner, now, now.plusSeconds(100))
      manuallyInsertLock(manualyInsertedLock)

      await(lockKeeper.tryLock {
        fail("Should not be run!")
      }) shouldBe None

      await(repo.findAll()).head shouldBe manualyInsertedLock
    }

    "return false from isLocked if no lock obtained" in {
      await(lockKeeper.isLocked) shouldBe false
    }

    "return true from isLocked if lock held" in {
      manuallyInsertLock(Lock(lockId, owner, now, now.plusSeconds(100)))
      await(lockKeeper.isLocked) shouldBe true
    }
  }


  "Mongo should" should {
    val DuplicateKey = 11000
    "throw an exception if a lock object is inserted that is not unique" in {
      val lock1 = Lock("lockName", "owner1", now.plusDays(1), now.plusDays(2))
      val lock2 = Lock("lockName", "owner2", now.plusDays(3), now.plusDays(4))
      manuallyInsertLock(lock1)

      val error = the[DefaultWriteResult] thrownBy manuallyInsertLock(lock2)
      error.code should contain(DuplicateKey)

      await(repo.findAll()).head shouldBe lock1
    }
  }
}

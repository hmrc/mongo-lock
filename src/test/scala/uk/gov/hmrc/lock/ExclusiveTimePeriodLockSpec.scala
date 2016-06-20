/*
 * Copyright 2016 HM Revenue & Customs
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
import org.joda.time.Duration
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

  class ExclusiveTimePeriodLockSpec extends WordSpecLike with Matchers with BeforeAndAfterEach with MongoSpecSupport with ScalaFutures with IntegrationPatience {

    val lockRepo = new LockRepository
    var executionChecker = 0

    override protected def beforeEach(): Unit = {
      lockRepo.removeAll().futureValue
      executionChecker = 0
    }

    "tryToAcquireOrRenewLock" should {

      val lock = new ExclusiveTimePeriodLock {
        override def lockId = "lockId"

        override def repo: LockRepository = lockRepo
      }

      val lock2 = new ExclusiveTimePeriodLock {
        override def lockId = "lockId"

        override def repo: LockRepository = lockRepo
      }

      val timeout: Duration = new Duration(1000)
      def increment: Future[Unit] = Future.successful(executionChecker += 1)

      "execute the body if no previous lock is set" in {

        lock.tryToAcquireOrRenewLock(timeout) {
          increment
        }.futureValue
        executionChecker shouldBe 1
      }

      "execute the body if the lock for same serverId exists" in {

        lock.tryToAcquireOrRenewLock(timeout) {
          increment
        }.futureValue

        lock.tryToAcquireOrRenewLock(timeout) {
          increment
        }.futureValue

        executionChecker shouldBe 2
      }

      "not execute the body and exit if the lock for another serverId exists" in {

        lock.tryToAcquireOrRenewLock(timeout) {
          increment
        }.futureValue

        lock2.tryToAcquireOrRenewLock(timeout) {
          increment
        }.futureValue

        executionChecker shouldBe 1

      }
      "execute the body if run after the keepLockFor time expired" in {

        lock.tryToAcquireOrRenewLock(timeout) {
          increment
        }.futureValue

        Thread.sleep(timeout.getMillis + 1)

        lock2.tryToAcquireOrRenewLock(timeout) {
          increment
        }.futureValue

        executionChecker shouldBe 2
      }
    }

  }

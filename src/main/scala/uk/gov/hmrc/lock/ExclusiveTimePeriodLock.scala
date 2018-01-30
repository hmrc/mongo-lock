/*
 * Copyright 2018 HM Revenue & Customs
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

import java.util.UUID

import org.joda.time.Duration

import scala.concurrent.{ExecutionContext, Future}

trait ExclusiveTimePeriodLock {

  def repo: LockRepository
  def lockId: String

  def holdLockFor: Duration

  lazy val serverId: String = UUID.randomUUID().toString

  def tryToAcquireOrRenewLock[T](body: => Future[T])(implicit ec: ExecutionContext): Future[Option[T]] = {

    val myFutureLock = for {
      renewed <- repo.renew(lockId, serverId, holdLockFor)
      acquired <- if (!renewed) repo.lock(lockId, serverId, holdLockFor) else Future.successful(false)
    } yield renewed || acquired

    myFutureLock.flatMap {
      case true => body.map(x => Some(x))
      case false => Future.successful(None)
    } recoverWith {
      case ex => repo.releaseLock(lockId, serverId).flatMap(_ => Future.failed(ex))
    }
  }
}

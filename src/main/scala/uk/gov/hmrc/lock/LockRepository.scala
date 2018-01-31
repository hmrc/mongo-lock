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

import org.joda.time.{DateTime, Duration}
import play.api.Logger
import play.api.libs.json._
import reactivemongo.api.DB
import reactivemongo.api.commands.{DefaultWriteResult, LastError}
import reactivemongo.bson.{BSONDateTime, BSONDocument}
import reactivemongo.core.commands.{FindAndModify, Update}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object LockFormats {

  case class Lock(id: String,
                  owner: String,
                  timeCreated: DateTime,
                  expiryTime: DateTime)

  implicit val dateFormat = ReactiveMongoFormats.dateTimeFormats

  implicit val format = ReactiveMongoFormats.mongoEntity(Format(Json.reads[Lock], Json.writes[Lock]))

  val id = "_id"
  val owner = "owner"
  val timeCreated = "timeCreated"
  val expiryTime = "expiryTime"

}

object LockMongoRepository {

  def apply(implicit mongo: () => DB) = new LockRepository()

}

class LockRepository(implicit mongo: () => DB) extends ReactiveRepository[LockFormats.Lock, String]("locks", mongo, LockFormats.format, implicitly[Format[String]]) {

  import uk.gov.hmrc.lock.LockFormats._
  import reactivemongo.play.json.ImplicitBSONHandlers._

  private val DuplicateKey = 11000

  def lock(reqLockId: String, reqOwner: String, forceReleaseAfter: Duration): Future[Boolean] = withCurrentTime { now =>
    collection.remove(Json.obj(id -> reqLockId, expiryTime -> Json.obj("$lte" -> now))) flatMap { writeResult =>
      if (writeResult.n != 0) {
        Logger.warn(s"Removed ${writeResult.n} expired locks for $reqLockId")
      }

      insert(Lock(reqLockId, reqOwner, now, now.plus(forceReleaseAfter))).map { _ =>
          Logger.debug(s"Took lock '$reqLockId' for '$reqOwner' at $now. Expires at: ${now.plus(forceReleaseAfter)}")
          true
        }
        .recover {
          case s @ LastError(_, _, Some(DuplicateKey), _, _, _, _, _, _, _, _, _, _, _) =>
            Logger.debug(s"Unable to take lock '$reqLockId' for '$reqOwner'")
            false
        }

    }
  }

  def renew(reqLockId: String, reqOwner: String, forceReleaseAfter: Duration): Future[Boolean] = withCurrentTime { now =>
    val expiryTime = now.plus(forceReleaseAfter)

    val selector = BSONDocument(id -> reqLockId, owner -> reqOwner, "expiryTime" -> BSONDocument("$gte" -> BSONDateTime(now.getMillis)))
    val modifier = BSONDocument("$set" -> BSONDocument("expiryTime" -> BSONDateTime(expiryTime.getMillis)))

    // Use findAndModify to ensure the read and the write are performed as one atomic operation
    collection.findAndUpdate(
      selector = selector,
      update = modifier,
      fetchNewObject = false
    ).map { r => r.value match {
        case None =>
          Logger.debug(s"Could not renew lock '$reqLockId' for '$reqOwner' that does not exist or has expired")
          false
        case Some(_) =>
          Logger.debug(s"Renewed lock '$reqLockId' for '$reqOwner' at $now.  Expires at: $expiryTime")
          true
      }
    }.recover {
      case LastError(_, _, Some(DuplicateKey), _, _, _, _, _, _, _, _, _, _, _) =>
        Logger.debug(s"Unable to renew lock '$reqLockId' for '$reqOwner'")
        false
    }

  }

  def releaseLock(reqLockId: String, reqOwner: String) = {
    Logger.debug(s"Releasing lock '$reqLockId' for '$reqOwner'")
    collection.remove(Json.obj(id -> reqLockId, owner -> reqOwner)).map(_ => ())
  }

  def isLocked(reqLockId: String, reqOwner: String) = withCurrentTime { now =>
    collection.find(Json.obj(id -> reqLockId, owner -> reqOwner, expiryTime -> Json.obj("$gt" -> now))).one[JsValue].map(_.isDefined)
  }

}

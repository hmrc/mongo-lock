
mongo-lock
====
[![Build Status](https://travis-ci.org/hmrc/mongo-lock.svg?branch=master)](https://travis-ci.org/hmrc/mongo-lock) [ ![Download](https://api.bintray.com/packages/hmrc/releases/mongo-lock/images/download.svg) ](https://bintray.com/hmrc/releases/mongo-lock/_latestVersion)

#Mongo-Lock

This is a utility that prevents multiple instances of the same application to perform an operation at the same time.
This can be useful of example when a REST api has to be called at a scheduled time. 
Without this utility every instance of the application would call the REST api.

There are 2 variants that can be used to instigate a lock, LockKeeper for locking for a particular task and ExclusiveTimePeriodLock to lock exclusively for a given time period (i.e. stop other instances executing the task until I stop renewing the lock)

The utility uses mongodb to create a lock

##Usage


1. Get access to mongodb
```
    val connection = {

          import play.api.Play.current
          ReactiveMongoPlugin.mongoConnector.db
   }
   val repo = LockMongoRepository(connection)
```

2. Create a LockKeeper or ExclusiveTimePeriodLock.

For LockKeeper the forceLockRelease timeout allows other apps to release and get the lock if it was stuck for some reasons
```
    val lockKeeper = new LockKeeper {
    
        override def repo: LockRepository = repo //The repo created before
    
        override def lockId: String = "<something unique for the operation>"
    
        override val forceLockReleaseAfter: Duration = Duration.standardMinutes(5)
    }
```

For ExclusiveTimePeriodLock the holdLockFor timeout allows other apps to claim the lock if it is not renewed for this period
```
    val exclusiveTimePeriodLock = new ExclusiveTimePeriodLock {

        override def repo: LockRepository = repo //The repo created before

        override def lockId: String = "<something unique for the operation>"

        override val holdLockFor: Duration = Duration.standardMinutes(5)
    }
```

3. Use the LockKeeper or ExclusiveTimePeriodLock to execute the code
```
 lockKeeper.tryLock { 
    // This will be executed on one application only
 }
```

```
 exclusiveTimePeriodLock.tryToAcquireOrRenew {
    // This will be executed on one application only
 }
```

The function in LockKeeper and ExclusiveTimePeriodLock accepts anything that returns a Future[T]
tryLock and tryToAcquireOrRen will return the result in an Option.
If it was not possible to acquire the lock, None is returned

```
def tryLock[T](body: => Future[T]): Future[Option[T]] 
def tryToAcquireOrRenew[T](body: => Future[T]): Future[Option[T]]
```

## Installing

Include the following dependency in your SBT build

``` scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies += "uk.gov.hmrc" %% "mongo-lock" % "x.x.x"
```

* For Play 2.5.x and simple-reactivemongo 7.x.x use versions >= 6.x.x-play-25
* For Play 2.6.x and simple-reactivemongo 7.x.x use versions >= 6.x.x-play-26
* For simple-reactivemongo below 7.x.x use versions < 6.0.0

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

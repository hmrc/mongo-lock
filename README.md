
mongo-lock
====
[![Build Status](https://travis-ci.org/hmrc/mongo-lock.svg?branch=master)](https://travis-ci.org/hmrc/mongo-lock) [ ![Download](https://api.bintray.com/packages/hmrc/releases/mongo-lock/images/download.svg) ](https://bintray.com/hmrc/releases/mongo-lock/_latestVersion)

#Mongo-Lock

This is a utility that prevents multiple instances of the same application to perform an operation at the same time.
This can be useful of example when a REST api has to be called at a scheduled time. 
Without this utility every instance of the application would call the REST api.

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
2. Create a LockKeeper. The forceLockRelease timeout allows other apps to release and get the lock if it was stuck for some reasons
```
    val lockKeeper = new LockKeeper {
    
        override def repo: LockRepository = repo //The repo created before
    
        override def lockId: String = "<something unique for the operation>"
    
        override val forceLockReleaseAfter: Duration = Duration.standardMinutes(5)
    }
```
3. Use the LockKeeper to execute the code
```
 lockKeeper.tryLock { 
    // This will be executed on one application only
 }
```


The function in LockKeeper accepts anything that returns a Future[T]
tryLock will return the result in an Option.
If it was not possible to acquire the lock, None is returned

```
def tryLock[T](body: => Future[T]): Future[Option[T]] 
```

## Installing

Include the following dependency in your SBT build

``` scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies += "uk.gov.hmrc" %% "mongo-lock" % "x.x.x"
```

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

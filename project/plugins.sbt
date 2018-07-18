resolvers += Resolver.bintrayIvyRepo("hmrc", "sbt-plugin-releases")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.12.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.6.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.15")

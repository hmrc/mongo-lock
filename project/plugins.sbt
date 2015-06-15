resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe repository mwn" at "http://repo.typesafe.com/typesafe/maven-releases/"

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "0.8.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "0.4.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.4")
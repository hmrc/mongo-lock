import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {

  import BuildDependencies._
  import uk.gov.hmrc.DefaultBuildSettings._

  val appName = "mongo-lock"

  lazy val mongoLock = (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      scalaVersion := "2.11.7",
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      ),
      libraryDependencies ++= Seq(
        Compile.playFramework,
        Compile.playReactiveMongo,
        Compile.time,
        Test.reactiveMongoTest,
        Test.scalaTest,
        Test.pegdown
      ),
      Developers()
    )
}

private object BuildDependencies {
  import play.core.PlayVersion

  object Compile {
    val playFramework = "com.typesafe.play" %% "play" % PlayVersion.current
    val playReactiveMongo = "uk.gov.hmrc" %% "play-reactivemongo" % "4.3.0" % "provided"
    val time = "uk.gov.hmrc" %% "time" % "2.0.0"
  }

  sealed abstract class Test(scope: String) {
    val reactiveMongoTest = "uk.gov.hmrc" %% "reactivemongo-test" % "1.1.0" % scope
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % scope
    val pegdown = "org.pegdown" % "pegdown" % "1.5.0" % scope
  }

  object Test extends Test("test")

}

object Developers {

  def apply() = developers := List[Developer]()
}


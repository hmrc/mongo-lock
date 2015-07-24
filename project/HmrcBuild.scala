import play.core.PlayVersion
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
      targetJvm := "jvm-1.7",
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      ),
      libraryDependencies ++= Seq(
        Compile.playFramework,
        Compile.scalaLogging,
        Compile.playReactiveMongo,
        Compile.simpleReactiveMongo,
        Compile.time,
        Test.scalaTest,
        Test.pegdown
      ),
      Developers()
    )
}

private object BuildDependencies {

  private val playReactivemongoVersion = "4.0.0"
  private val simpleReactivemongoVersion = "3.0.0"

  object Compile {
    val playFramework = "com.typesafe.play" %% "play" % PlayVersion.current
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.0.0"
    val playReactiveMongo = "uk.gov.hmrc" %% "play-reactivemongo" % playReactivemongoVersion % "provided"
    val simpleReactiveMongo = "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion % "provided"
    val time = "uk.gov.hmrc" %% "time" % "1.4.0"
  }

  sealed abstract class Test(scope: String) {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % scope
    val pegdown = "org.pegdown" % "pegdown" % "1.5.0" % scope
  }

  object Test extends Test("test")

}

object Developers {

  def apply() = developers := List[Developer]()
}


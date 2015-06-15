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
      libraryDependencies ++= Seq(
        Compile.playFramework,
        Compile.scalaLogging,
        Compile.playReactiveMongo,
        Compile.simpleReactiveMongo,
        Compile.time,
        Test.scalaTest,
        Test.pegdown,
        Test.simpleReactiveMongo
      )
      ,
      Developers()
    )
}

private object BuildDependencies {

  private val playReactivemongoVersion = "3.4.1"
  private val simpleReactivemongoVersion = "2.6.1"

  object Compile {
    val playFramework = "com.typesafe.play" %% "play" % PlayVersion.current
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.0.0"
    val playReactiveMongo = "uk.gov.hmrc" %% "play-reactivemongo" % playReactivemongoVersion
    val simpleReactiveMongo = "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion
    val time = "uk.gov.hmrc" %% "time" % "1.3.0"
  }

  sealed abstract class Test(scope: String) {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % scope
    val pegdown = "org.pegdown" % "pegdown" % "1.5.0" % scope
    val simpleReactiveMongo = "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion % scope classifier "tests"
  }

  object Test extends Test("test")

}

object Developers {

  def apply() = developers := List[Developer]()
}


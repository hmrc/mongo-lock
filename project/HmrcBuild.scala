import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning


object HmrcBuild extends Build {

  val appName = "mongo-lock"

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(defaultSettings(): _*)
    .settings(
      scalaVersion := "2.11.8",
      libraryDependencies ++= AppDependencies(),
      crossScalaVersions := Seq("2.11.8"),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
      )
    )
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
}


private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  val compile = Seq(
    filters,
    "com.typesafe.play" %% "play" % PlayVersion.current % "provided",
    "uk.gov.hmrc" %% "simple-reactivemongo" % "5.2.0",
    "uk.gov.hmrc" %% "time" % "3.0.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % "3.0.1" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}

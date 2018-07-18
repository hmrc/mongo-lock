import uk.gov.hmrc.DefaultBuildSettings.defaultSettings
import SbtGitVersioning.majorVersion

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)

name := "mongo-lock"

majorVersion := 5

defaultSettings()

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
  "-language:reflectiveCalls"
)

scalaVersion := "2.11.12"

libraryDependencies ++= AppDependencies()

crossScalaVersions := Seq("2.11.12")

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.typesafeRepo("releases")
)

disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

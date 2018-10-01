import PlayCrossCompilation._
import uk.gov.hmrc.DefaultBuildSettings.defaultSettings

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)

name := "mongo-lock"

makePublicallyAvailableOnBintray := true

majorVersion                     := 6

defaultSettings()

scalaVersion := "2.11.8"

libraryDependencies ++= LibDependencies()

crossScalaVersions := Seq("2.11.8")

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
)

playCrossCompilationSettings

disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

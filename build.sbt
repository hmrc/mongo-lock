import uk.gov.hmrc.DefaultBuildSettings.defaultSettings

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)

name := "mongo-lock"

defaultSettings()

scalaVersion := "2.11.8"

libraryDependencies ++= AppDependencies()

crossScalaVersions := Seq("2.11.8")

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
)

disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

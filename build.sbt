name := "mongo-lock"

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

makePublicallyAvailableOnBintray := true

majorVersion := 7

scalaVersion := "2.12.13"

libraryDependencies ++= LibDependencies()

resolvers += Resolver.typesafeRepo("releases")

PlayCrossCompilation.playCrossCompilationSettings

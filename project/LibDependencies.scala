import sbt._

object LibDependencies {
  
  def apply(): Seq[ModuleID] = compile ++ test

  private val play25Version = "2.5.12"
  private val play26Version = "2.6.15"

  private val compile: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "uk.gov.hmrc" %% "time" % "3.0.0"
    ),
    play25 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play25Version,
      "com.typesafe.play" %% "play"                 % play25Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.0.0-play-25"
    ),
    play26 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play26Version,
      "com.typesafe.play" %% "play"                 % play26Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.0.0-play-26"
    )
  )

  private val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.pegdown"   % "pegdown"    % "1.6.0" % Test
    ),
    play25 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "4.1.0-play-25" % Test,
      "ch.qos.logback" % "logback-classic"     % "1.1.2"         % Test
    ),
    play26 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "4.1.0-play-26" % Test,
      "ch.qos.logback" % "logback-classic"     % "1.2.3"         % Test
    )
  )
}

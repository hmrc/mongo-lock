import sbt._

object LibDependencies {

  def apply(): Seq[ModuleID] = compile ++ test

  private val play26Version = "2.6.25"
  private val play27Version = "2.7.9"
  private val play28Version = "2.8.7"

  private val compile: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "uk.gov.hmrc" %% "time" % "3.3.0"
    ),
    play26 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play26Version,
      "com.typesafe.play" %% "play"                 % play26Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "8.0.0-play-26"
    ),
    play27 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play27Version,
      "com.typesafe.play" %% "play"                 % play27Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "8.0.0-play-27"
    ),
    play28 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play28Version,
      "com.typesafe.play" %% "play"                 % play28Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "8.0.0-play-28"
    )
  )

  private val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "org.scalatest"        %% "scalatest"       % "3.1.4"  % Test,
      "com.vladsch.flexmark" %  "flexmark-all"    % "0.36.8" % Test,
      "ch.qos.logback"       %  "logback-classic" % "1.2.3"  % Test
    ),
    play26 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "5.0.0-play-26" % Test
    ),
    play27 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "5.0.0-play-27" % Test
    ),
    play28 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "5.0.0-play-28" % Test
    )
  )
}

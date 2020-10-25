import sbt._

object LibDependencies {

  def apply(): Seq[ModuleID] = compile ++ test

  private val play25Version = "2.5.19"
  private val play26Version = "2.6.23"
  private val play27Version = "2.7.4"

  private val compile: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "uk.gov.hmrc" %% "time" % "3.3.0",
      // force dependencies due to security flaws found in jackson-databind < 2.9.x using XRay
      "com.fasterxml.jackson.core"     % "jackson-core"            % "2.9.7",
      "com.fasterxml.jackson.core"     % "jackson-databind"        % "2.9.7",
      "com.fasterxml.jackson.core"     % "jackson-annotations"     % "2.9.7",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"   % "2.9.7",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.7"
    ),
    play25 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play25Version,
      "com.typesafe.play" %% "play"                 % play25Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.31.0-play-25-SNAPSHOT",
      // force dependencies due to security flaws found in xercesImpl 2.11.0
      // only applies to play 2.5 since it was removed from play 2.6
      // https://github.com/playframework/playframework/blob/master/documentation/manual/releases/release26/migration26/Migration26.md#xercesimpl-removal
      "xerces" % "xercesImpl" % "2.12.0"
    ),
    play26 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play26Version,
      "com.typesafe.play" %% "play"                 % play26Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.31.0-play-26-SNAPSHOT"
    ),
    play27 = Seq(
      "com.typesafe.play" %% "filters-helpers"      % play27Version,
      "com.typesafe.play" %% "play"                 % play27Version,
      "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.31.0-play-27-SNAPSHOT"
    )
  )

  private val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "org.scalatest"  %% "scalatest"       % "3.0.5" % Test,
      "org.pegdown"    %  "pegdown"         % "1.6.0" % Test,
      "ch.qos.logback" %  "logback-classic" % "1.2.3" % Test
    ),
    play25 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "4.21.0-play-25" % Test
    ),
    play26 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "4.21.0-play-26" % Test
    ),
    play27 = Seq(
      "uk.gov.hmrc"    %% "reactivemongo-test" % "4.21.0-play-27" % Test
    )
  )
}

import sbt._

object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile = Seq(
    filters,
    "com.typesafe.play" %% "play" % PlayVersion.current % "provided",
    // TODO: Change this to reflect released version
    "uk.gov.hmrc" %% "simple-reactivemongo-26" % "0.3.0",
    "uk.gov.hmrc" %% "time" % "3.0.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    def test: Seq[ModuleID]
  }

  object Test {
    def apply() = new TestDependencies {
      override val test = Seq(
        "org.scalatest" %% "scalatest" % "3.0.4" % Test,
        "org.pegdown" % "pegdown" % "1.6.0" % Test,
        "uk.gov.hmrc" %% "reactivemongo-test" % "3.0.0" % Test
      )
    }.test
  }

  def apply() = compile ++ Test()
}

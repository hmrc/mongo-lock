import sbt._

object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

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

import uk.gov.hmrc.playcrosscompilation.AbstractPlayCrossCompilation
import uk.gov.hmrc.playcrosscompilation.PlayVersion

object PlayCrossCompilation extends AbstractPlayCrossCompilation(defaultPlayVersion = PlayVersion.Play25) {
  override def playCrossScalaBuilds(scalaVersions: Seq[String]): Seq[String] =
    playVersion match {
      case PlayVersion.Play25 => scalaVersions.filter(version => version.startsWith("2.11"))
      case PlayVersion.Play26 => scalaVersions
      case PlayVersion.Play27 => // simple-reactivemongo is only available for 2.12 (akka incompatibility)
                                 scalaVersions.filter(version => version.startsWith("2.12"))
    }
}

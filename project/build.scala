import sbt._
import Keys._
import Common._
import Dependencies._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys

object build extends Build {

  private[this] val coreName = "dog-autodoc-core"
  private[this] val allName = "dog-autodoc-all"

  private[this] def module(id: String) =
    Project(id, file(id)).settings(commonSettings)

  val modules: List[String] = (
    coreName ::
    allName ::
    Nil
  )

  lazy val core = module("core").settings(
    name := coreName,
    libraryDependencies ++= Seq(
      dogCore,
      httpz
    ),
    TwirlKeys.templateImports ++= Seq(
        "dog.autodoc.{ RequestDocument, ResponseDocument }"
      ),
      TwirlKeys.templateFormats += ("md" -> "dog.twirl.MarkdownFormat")
  ).enablePlugins(SbtTwirl)

  val root = Project("root", file(".")).settings(
    commonSettings
  ).settings(
    name := allName,
    packagedArtifacts := Map.empty
  ).aggregate(
    core
  )
}

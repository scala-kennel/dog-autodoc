import build._
import Dependencies._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys

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

lazy val autodoc = module("autodoc").settings(
  name := autodocName,
  libraryDependencies ++= Seq(
    dogLib
  )
).dependsOn(core)

val root = Project("root", file(".")).settings(
  Common.commonSettings
).settings(
  name := allName,
  packagedArtifacts := Map.empty
).aggregate(
  core, autodoc
)

import sbt._
import Keys._
import Common._
import Dependencies._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys
import dog.DogPlugin.autoImport._

object build extends Build {

  private[this] val coreName = "dog-autodoc-core"
  private[this] val autodocName = "dog-autodoc"
  private[this] val pluginName = "sbt-dog-autodoc"
  private[this] val allName = "dog-autodoc-all"

  private[this] def module(id: String) =
    Project(id, file(id)).settings(commonSettings)

  val modules: List[String] = (
    coreName ::
    autodocName ::
    pluginName ::
    allName ::
    Nil
  )

  private[this] val scala211 = "2.11.7"

  lazy val core = module("core").settings(
    dogSettings
  ).settings(
    name := coreName,
    scalaVersion := scala211,
    crossScalaVersions := Seq("2.10.5", scala211),
    dogVersion := Dependencies.Version.dog,
    description := "autodoc for dog",
    libraryDependencies ++= Seq(
      dogCore,
      httpz
    ),
    TwirlKeys.templateImports ++= Seq(
      "dog.autodoc.{ RequestDocument, ResponseDocument }"
    ),
    TwirlKeys.templateFormats += ("md" -> "dog.twirl.MarkdownFormat")
  ).enablePlugins(SbtTwirl)

  lazy val autodoc = module(autodocName).settings(
    dogSettings
  )
  .settings(
    name := autodocName,
    scalaVersion := scala211,
    crossScalaVersions := Seq("2.10.5", scala211),
    dogVersion := Dependencies.Version.dog,
    description := "autodoc for dog",
    libraryDependencies ++= Seq(
      scalazStream
    )
  ).dependsOn(core)

  lazy val plugin = module(pluginName).settings(
    ScriptedPlugin.scriptedSettings
  ).settings(
    name := pluginName,
    sbtPlugin := true,
    description := "sbt plugin for dog-autodoc",
    ScriptedPlugin.scriptedBufferLog := false,
    ScriptedPlugin.scriptedLaunchOpts ++= sys.process.javaVmArguments.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
    ),
    ScriptedPlugin.scriptedLaunchOpts ++= Seq(
      s"-Dplugin.version=${version.value}",
      s"-DdogAutodoc.version=${version.value}"
    )
  )

  val root = Project("root", file(".")).settings(
    commonSettings
  ).settings(
    name := allName,
    packagedArtifacts := Map.empty
  ).aggregate(
    core, autodoc, plugin
  )
}

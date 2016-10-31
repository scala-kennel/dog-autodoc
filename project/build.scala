import sbt._
import Keys._
import Common._
import Dependencies._

object build {

  val coreName = "dog-autodoc-core"
  val autodocName = "dog-autodoc"
  val allName = "dog-autodoc-all"

  def module(id: String) =
    Project(id, file(id)).settings(commonSettings)

  val modules: List[String] = (
    coreName ::
    autodocName ::
    allName ::
    Nil
  )
}

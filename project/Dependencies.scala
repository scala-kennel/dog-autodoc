import sbt._, Keys._

object Dependencies {

  object Version {
    val dog = "0.8.0"
    val httpz = "0.5.1"
  }

  val dogCore = "com.github.pocketberserker" %% "dog-core" % Version.dog
  val dogLib = "com.github.pocketberserker" %% "dog" % Version.dog
  val httpz = "com.github.xuwei-k" %% "httpz" % Version.httpz
}

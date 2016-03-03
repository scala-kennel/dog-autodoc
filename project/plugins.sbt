addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.4.0")
addSbtPlugin("com.github.pocketberserker" % "sbt-dog" % "0.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.1.1")

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  "-Yno-adapted-args" ::
  Nil
)


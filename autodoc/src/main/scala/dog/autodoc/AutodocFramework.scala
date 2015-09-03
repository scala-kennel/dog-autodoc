package dog
package autodoc

import sbt.testing.{Fingerprint, Framework}

class AutodocFramework extends Framework {

  override def name() = "Autodoc"

  override def fingerprints() = Array[Fingerprint](DogFingerprint)

  override def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader) =
    new AutodocRunner(args, remoteArgs, testClassLoader)
}

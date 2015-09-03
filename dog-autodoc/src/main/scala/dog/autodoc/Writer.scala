package dog
package autodoc

import scalaz.{\/-, NonEmptyList}
import scalaz.stream._
import scalaz.concurrent.Task

object Writer {

  def write[A](tests: Seq[(String, TestResult[AutodocMarker])], className: String, outputDir: String): Unit = {
    val fileName = className.replaceAll("Test|Spec", "")
    (Process(tests: _*): Process[Task, (String, TestResult[AutodocMarker])])
      .collect { case (title, Done(NonEmptyList(\/-(x), _ @ _*))) => x.generate(title) }
      .pipe(text.utf8Encode)
      .to(io.fileChunkW(s"$outputDir/$fileName.md"))
      .run
      .run
  }
}

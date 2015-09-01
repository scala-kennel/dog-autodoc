package dog
package autodoc

import scalaz.{\/-, NonEmptyList}
import scalaz.stream._
import scalaz.concurrent.Task

object Writer {

  def write[A](tests: Seq[(String, TestResult[Autodoc[A]])], className: String, directoryPath: String): Unit =
    (Process(tests: _*): Process[Task, (String, TestResult[Autodoc[A]])])
      .collect { case (title, Done(NonEmptyList(\/-(x), _ @ _*))) => x.generate(title) }
      .pipe(text.utf8Encode)
      .to(io.fileChunkW(s"$directoryPath/$className.md"))
      .run
      .run
}

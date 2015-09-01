package dog
package autodoc

import java.nio.file._
import scalaz.stream._

object WriterTest extends Dog {

  val writeDoc = TestCase.delay {
    val test = for {
      doc <- AutodocTest.toMarkdown
      _ <- TestCase.delay {
        val title = "GET /api"
        Writer.write(Seq((title, TestResult(doc))), "Api", "doc")
        val actual = io.linesR("doc/Api.md")
          .runLog
          .run
          .mkString("\n")
        Files.delete(Paths.get("doc/Api.md"))
        Assert.equal(doc.generate(title), actual)
      }
    } yield ()
    test
  }
}

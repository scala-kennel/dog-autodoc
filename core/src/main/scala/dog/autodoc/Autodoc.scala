package dog
package autodoc

import scalaz._, Id._
import httpz._
import argonaut.DecodeJson

trait AutodocMarker {
  def generate(title: String, format: Autodoc.Format): String
}

sealed abstract class Autodoc[A: Show] extends AutodocMarker {

  def description: Option[String]

  def request: Request

  def response: Response[A]

  // not Functor
  def map[B: Show](f: A => B): Autodoc[B]
}

object Autodoc {

  sealed abstract class Format
  case object Markdown extends Format
  final case object Html extends Format

  def apply[A: Show](interpreter: Interpreter[Id], p: ActionNel[Autodoc[A]])
    (test: Response[A] => TestCase[Unit]): TestCase[Autodoc[A]] = TestCase.delay {
    val r = interpreter.run(p)
    r match {
      case -\/(es) => TestCase(TestResult.error(es.list, List()))
      case \/-(a) => test(a.response).map(Function.const(a))
    }
  }

  private[this] final case class AutodocImpl[A: Show](
    override val description: Option[String],
    override val request: Request,
    override val response: Response[A]) extends Autodoc[A] {

    override def generate(title: String, format: Format) = {
      val req = RequestDocument.from(request)
      val res = ResponseDocument.from(implicitly[Show[A]].show(response))
      format match {
        case Markdown =>
          dog.autodoc.templates.md.document(title, description, req, res).body.trim
        case Html =>
          html.document(title, description, req, res).body.trim
      }
    }

    override def map[B: Show](f: A => B) = AutodocImpl(description, request, response.map(f))
  }

  private[this] def descriptionOption[A](description: String): Option[String] =
    if(description.trim.isEmpty) None else Some(description)

  def json[A <: JsonToString[A]: DecodeJson](req: Request, description: String = "") =
    Core.jsonResponse(req).map[Autodoc[A]](res =>
      AutodocImpl(descriptionOption(description), req, res))

  def string(req: Request, description: String = "") =
    Core.stringResponse(req).map[Autodoc[String]](res =>
      AutodocImpl(descriptionOption(description), req, res))

  def raw(req: Request, description: String = "") =
    Core.raw(req).map[Autodoc[ByteArray]](res =>
      AutodocImpl(descriptionOption(description), req, res))
}

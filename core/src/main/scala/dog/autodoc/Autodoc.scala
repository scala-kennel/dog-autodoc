package dog
package autodoc

import scalaz._, Id._
import httpz._
import argonaut.DecodeJson

trait AutodocMarker {
  def generate(title: String, format: Autodoc.Format): String
}

final case class Autodoc[A: Show] private (
  description: Option[String],
  request: Request,
  response: Response[A]) extends AutodocMarker {

  override def generate(title: String, format: Autodoc.Format) = {
    val req = RequestDocument.from(request)
    val res = ResponseDocument.from(implicitly[Show[A]].show(response))
    format match {
      case Autodoc.Markdown =>
        dog.autodoc.templates.md.document(title, description, req, res).body.trim
      case Autodoc.Html =>
        html.document(title, description, req, res).body.trim
    }
  }
}

object Autodoc {

  sealed abstract class Format
  case object Markdown extends Format
  final case object Html extends Format

  def apply[A: Show](interpreter: Interpreter[Id], p: ActionNel[Autodoc[A]])
    (test: Response[A] => TestCase[Unit]): TestCase[Autodoc[A]] = {
    val r = interpreter.run(p)
    r match {
      case -\/(es) => TestCase(TestResult.error(es.list, List()))
      case \/-(a) => test(a.response).map(Function.const(a))
    }
  }

  private[this] def descriptionOption[A](description: String): Option[String] =
    if(description.trim.isEmpty) None else Some(description)

  def json[A <: JsonToString[A]: DecodeJson](req: Request, description: String = "") =
    Core.jsonResponse(req).map(res =>
      Autodoc(descriptionOption(description), req, res))

  def string(req: Request, description: String = "") =
    Core.stringResponse(req).map(res =>
      Autodoc(descriptionOption(description), req, res))

  def raw(req: Request, description: String = "") =
    Core.raw(req).map(res =>
      Autodoc(descriptionOption(description), req, res))
}

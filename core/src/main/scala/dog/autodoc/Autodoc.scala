package dog
package autodoc

import httpz._
import argonaut._

final case class Autodoc[A: Show](
  description: Option[String],
  request: Request,
  response: Response[A]) {

  def generate(title: String): String = {
    val req = RequestDocument.from(request)
    val res = ResponseDocument.from(implicitly[Show[A]].show(response))
    dog.autodoc.templates.md.document(title, description, req, res).body
  }
}

object Autodoc {

  def json[A: CodecJson](req: Request) =
    Core.jsonResponse(req).map(res => Autodoc(None, req, res))

  def string(req: Request) =
    Core.stringResponse(req).map(res => Autodoc(None, req, res))

  def raw(req: Request) =
    Core.raw(req).map(res => Autodoc(None, req, res))
}

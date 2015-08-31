package dog
package autodoc

import httpz._
import argonaut.{EncodeJson, DecodeJson}

final case class Autodoc[A](
  description: Option[String],
  request: Request,
  response: Response[A]) {

  def generate(title: String)(implicit A: EncodeJson[A]): String = {
    val req = RequestDocument.from(request)
    val res = ResponseDocument.from(response.map(v => A.encode(v).toString()))
    dog.autodoc.templates.md.document(title, description, req, res).body
  }
}

object Autodoc {

  def json[A: DecodeJson](req: Request) =
    Core.jsonResponse(req).map(res => Autodoc(None, req, res))

  def string(req: Request) =
    Core.stringResponse(req).map(res => Autodoc(None, req, res))

  def raw(req: Request) =
    Core.raw(req).map(res => Autodoc(None, req, res))
}

package dog
package autodoc

import httpz._
import argonaut.DecodeJson

trait AutodocMarker {
  type A
  def generate(title: String): String
  def toAutoDoc: Autodoc[A]
}

final case class Autodoc[A0: Show](
  description: Option[String],
  request: Request,
  response: Response[A0]) extends AutodocMarker {

  type A = A0

  override def generate(title: String) = {
    val req = RequestDocument.from(request)
    val res = ResponseDocument.from(implicitly[Show[A]].show(response))
    dog.autodoc.templates.md.document(title, description, req, res).body.trim
  }

  override def toAutoDoc = this
}

object Autodoc {

  def json[A <: JsonToString[A]: DecodeJson](req: Request) =
    Core.jsonResponse(req).map(res => Autodoc(None, req, res))

  def string(req: Request) =
    Core.stringResponse(req).map(res => Autodoc(None, req, res))

  def raw(req: Request) =
    Core.raw(req).map(res => Autodoc(None, req, res))
}

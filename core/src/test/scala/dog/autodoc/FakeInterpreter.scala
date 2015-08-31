package dog
package autodoc

import httpz._

final case class FakeInterpreter(
  body: ByteArray,
  status: Int,
  headers: Map[String, List[String]]) extends InterpretersTemplate {

  protected[this] def request2response(req: Request) =
    Response(body, status, headers)
}

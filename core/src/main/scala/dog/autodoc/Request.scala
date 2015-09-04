package dog
package autodoc

import java.net.URI
import httpz.Request

final case class RequestDocument(
  path: String,
  method: String,
  body: String,
  headers: String
)

object RequestDocument {

  def from(req: Request): RequestDocument = {
    val headers =
      if(req.headers.isEmpty) ""
      else {
        "\n" + (for {
          kvp <- req.headers
        } yield s"${kvp._1}: ${kvp._2}").mkString("\n")
      }
    val urlPath = new URI(req.url).getPath()
    val path =
      if(req.params.isEmpty) urlPath
      else {
        val query = (for {
          kvp <- req.params
        } yield s"${kvp._1}=${kvp._2}").mkString("&")
        s"${urlPath}?${query}"
      }
    val body = req.body match {
      case None => ""
      case Some(v) => "\n\n" + new String(v, "UTF-8")
    }
    RequestDocument(path, req.method, body, headers)
  }
}

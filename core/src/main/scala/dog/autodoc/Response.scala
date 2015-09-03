package dog
package autodoc

import httpz.Response

final case class ResponseDocument(
  body: String,
  status: Int,
  headers: String
)

object ResponseDocument {

  def from(res: Response[String]): ResponseDocument = {
    val headers =
      if(res.headers.isEmpty) ""
      else {
        "\n" + (for {
          kvp <- res.headers
        } yield s"""${kvp._1}: ${kvp._2.mkString("; ")}""").mkString("\n")
      }
    val body =
      if(res.body.trim.isEmpty) ""
      else s"\n\n${res.body}"
    ResponseDocument(body, res.status, headers)
  }
}

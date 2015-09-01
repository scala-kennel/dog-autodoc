package dog
package autodoc

import httpz._

object AutodocTest extends Dog {

  def str(value: String) = new ByteArray(value.getBytes())

  def interpreter(value: String, status: Int) =
    FakeInterpreter(str(value), status, Map()).sequential.empty

  val doc = Autodoc.string(Request(
      method = "GET",
      url = "http://localhost/api"
    )).leftMap(Error.http).nel

  val toMarkdown = {
    val expected = """## GET /api

#### Request
```
GET http://localhost/api
```

#### Response
```
200

"{}"
```"""
    for {
      doc <- autodoc.apply[String](interpreter("{}", 200), doc) { res =>
        Assert.equal(200, res.status)
      }
      _ <- Assert.equal(expected, doc.generate("GET /api"))
    } yield doc
  }
}

package dog
package autodoc

import httpz._
import argonaut._, Argonaut._

object AutodocTest extends DogAutodoc with Assert {

  def str(value: String) = new ByteArray(value.getBytes())

  def interpreter(value: String, status: Int, headers: Map[String, List[String]] = Map()) =
    FakeInterpreter(str(value), status, headers).sequential.empty

  def run[A: Show](nel: ActionNel[Autodoc[A]], value: String, status: Int, headers: Map[String, List[String]] = Map()) =
    Autodoc[A](interpreter(value, status, headers), nel) { res =>
      equal(200, res.status)
    }

  val getApi = Autodoc.string(Request(
      method = "GET",
      url = "http://localhost/api"
    )).leftMap(Error.http).nel

  val `simple GET api` = {
    val expected = """## GET /api

#### Request
```
GET /api
```

#### Response
```
200

"{}"
```"""
    for {
      doc <- run[String](getApi, "{}", 200)
      _ <-
        assert
          .equal(expected, doc.generate("GET /api", Autodoc.Markdown()))
          .lift
    } yield doc
  }

  val getApiWithDescription = Autodoc.string(Request(
      method = "GET",
      url = "http://localhost/api"
    ), "test api").leftMap(Error.http).nel

  val `include description` = {
    val expected = """## GET /api
test api

#### Request
```
GET /api
```

#### Response
```
200

"{}"
```"""
    for {
      doc <- Autodoc[String](interpreter("{}", 200), getApiWithDescription) { res =>
        equal(200, res.status)
      }
      _ <-
        assert
          .equal(expected, doc.generate("GET /api", Autodoc.Markdown()))
          .lift
    } yield doc
  }

  case class Person(name: String, age: Int) extends JsonToString[Person]

  implicit val personCodec: CodecJson[Person] = casecodec2(Person.apply, Person.unapply)("name", "age")

  val getPerson = Autodoc.json[Person](Request(
      method = "GET",
      url = "http://localhost/person/1"
    )).leftMap(Error.http).nel

  val `get json` = {
    val expected = """## GET /person/:id

#### Request
```
GET /person/1
```

#### Response
```
200

{
  "name" : "Alice",
  "age" : 17
}
```"""
    for {
      doc <- run[Person](getPerson, Person("Alice", 17).toString, 200)
      _ <-
        assert
          .equal(expected, doc.generate("GET /person/:id", Autodoc.Markdown()))
          .lift
    } yield doc
  }

  val getApiWithHeader = Autodoc.string(Request(
      method = "GET",
      url = "http://localhost/api",
      headers = Map("Content-Type" -> "text/plain")
    )).leftMap(Error.http).nel

  val `GET api with header` = {
    val expected = """## GET /api

#### Request
```
GET /api
Content-Type: text/plain
```

#### Response
```
200
X-XSS-Protection: 1; mode=block

"{}"
```"""
    for {
      doc <- run[String](getApiWithHeader, "{}", 200,
        Map("X-XSS-Protection" -> List("1", "mode=block"))
      )
      _ <-
        assert
          .equal(expected, doc.generate("GET /api", Autodoc.Markdown()))
          .lift
    } yield doc
  }

  val queryPerson = Autodoc.json[Person](Request(
      method = "GET",
      url = "http://localhost/persons",
      params = Map("foo" -> "bar", "a" -> "b")
    )).leftMap(Error.http).nel

  val `query json` = {
    val expected = """## GET /persons?foo=bar&a=b

#### Request
```
GET /persons?foo=bar&a=b
```

#### Response
```
200

{
  "name" : "Alice",
  "age" : 17
}
```"""
    for {
      doc <- run[Person](queryPerson, Person("Alice", 17).toString, 200)
      _ <-
        assert
          .equal(expected, doc.generate("GET /persons?foo=bar&a=b", Autodoc.Markdown()))
          .lift
    } yield doc
  }

  val `generate simple html` = {
    val expected = """<h2>GET /api</h2>

<h4>Request</h4>

<pre><code>GET /api
</code></pre>

<h4>Response</h4>

<pre><code>200

&quot;{}&quot;
</code></pre>"""
    for {
      doc <- run[String](getApi, "{}", 200)
      _ <-
        assert
          .equal(expected, doc.generate("GET /api", Autodoc.Html()))
          .lift
    } yield doc
  }
}

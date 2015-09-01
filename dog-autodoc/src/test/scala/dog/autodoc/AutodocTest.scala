package dog
package autodoc

import httpz._
import argonaut._, Argonaut._

object AutodocTest extends Dog {

  def str(value: String) = new ByteArray(value.getBytes())

  def interpreter(value: String, status: Int) =
    FakeInterpreter(str(value), status, Map()).sequential.empty

  val getApi = Autodoc.string(Request(
      method = "GET",
      url = "http://localhost/api"
    )).leftMap(Error.http).nel

  val `simple GET api` = {
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
      doc <- autodoc.apply[String](interpreter("{}", 200), getApi) { res =>
        Assert.equal(200, res.status)
      }
      _ <- Assert.equal(expected, doc.generate("GET /api"))
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
GET http://localhost/person/1
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
      doc <- autodoc.apply[Person](interpreter(Person("Alice", 17).toString, 200), getPerson) { res =>
        Assert.equal(200, res.status)
      }
      _ <- Assert.equal(expected, doc.generate("GET /person/:id"))
    } yield doc
  }
}

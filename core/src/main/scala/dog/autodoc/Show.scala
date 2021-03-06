package dog
package autodoc

import httpz._

// TODO: rename
trait Show[A] {

  def show(response: Response[A]): Response[String]
}

object Show {

  implicit def json[A <: JsonToString[A]]: Show[A] = new Show[A] {
    def show(response: Response[A]) = response.map(v => v.toString)
  }

  implicit val string: Show[String] = new Show[String] {
    def show(response: Response[String]) = response.map(v => "\"" + v + "\"")
  }

  implicit val raw: Show[ByteArray] = new Show[ByteArray] {
    def show(response: Response[ByteArray]) =
      response.map(_ =>
        response.headers.get("Content-Type")
          .map(_.mkString("\n"))
          .getOrElse("ByteArray is not showable.")
      )
  }
}

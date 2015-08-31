package dog

import scalaz._, Id._
import httpz._

package object autodoc {

  def apply[A: Show](interpreter: Interpreter[Id], p: ActionNel[Autodoc[A]], description: String = "")
    (test: Response[A] => TestCase[Unit]): TestCase[Autodoc[A]] = {
    val d = if(description.trim.isEmpty) None else Some(description)
    val r = interpreter.run(p)
    r match {
      case -\/(es) => TestCase(TestResult.error(es.list, List()))
      case \/-(a) => test(a.response).map(v => a.copy(description = d))
    }
  }
}

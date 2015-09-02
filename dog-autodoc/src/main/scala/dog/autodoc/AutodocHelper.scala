package dog
package autodoc

import java.lang.reflect.Method
import sbt.testing._

object AutodocHelper {

  def autodocFieldNames(clazz: Class[_]): Array[String] =
    findTestFields(clazz, classOf[TestCase[Autodoc[_]]]).map(_.getName)

  private[this] def findTestFields(clazz: Class[_], fieldType: Class[_]): Array[Method] =
    clazz.getMethods.filter(method =>
      method.getParameterTypes.length == 0 && method.getReturnType == fieldType
    )

  def filterAutodocOnly(clazz: Class[_], events: Seq[Event]): Seq[(String, TestResult[Autodoc[Any]])] = {
    def getName(e: Event) = e.selector match {
      case selector: TestSelector => selector.testName.split('.').last
      case _   => "(It is not a test)"
    }
    val names = autodocFieldNames(clazz)
    events.collect {
      case e: DogEvent[_] if names.exists(n => n == getName(e)) =>
        (getName(e), e.result.asInstanceOf[TestResult[Autodoc[Any]]])
    }
  }
}

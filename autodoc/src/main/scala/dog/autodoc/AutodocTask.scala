package dog
package autodoc

import sbt.testing._
import scalaz._
import scalaz.Kleisli._

private[autodoc] class AutodocTask(
  args: Array[String],
  override val taskdef: TaskDef,
  override val testClassName: String,
  testClassLoader: ClassLoader,
  tracer: DogTracer) extends DogTask(args, taskdef, testClassName, testClassLoader, tracer) {

  override def taskDef() = taskdef

  override def execute(eventHandler: EventHandler, loggers: Array[Logger]) = {

    val log = DogRunner.logger(loggers)

    val only = scalaz.std.list.toNel(
      args.dropWhile("--only" != _).drop(1).takeWhile(arg => !arg.startsWith("--")).toList
    )

    val clazz = testClassLoader.loadClass(testClassName + "$")
    val obj = clazz.getDeclaredField("MODULE$").get(null).asInstanceOf[Dog]
    val tests = DogRunner.allTests(clazz, obj, only, log)
    val results = tests.map { case (name, test) =>
      val selector = new TestSelector(name)
      def event(status: Status, duration: Long, result: TestResult[Any]): DogEvent[Any] =
        DogTask.event(this, status, selector, duration, result)

      def doc(value: Any, e: DogEvent[Any]): DogEvent[Any] = obj match {
        case autodoc: DogAutodoc =>
          value match {
            // XXX
            case v: AutodocMarker =>
              lazy val markdown = v.generate(name, autodoc.markdown)
              lazy val html = v.generate(name, autodoc.html)
              e.copy(throwable = new OptionalThrowable(new Exception(markdown, new Exception(html))))
            case _ => e
          }
        case _ => e
      }

      val executor = DogTask.executor(log)
      val param = obj.paramEndo compose Param.executor(executor)
      val start = System.currentTimeMillis()
      val r = try {
        obj.listener.onStart(obj, name, log)
        val r = test.fold(
          _.foldMap(obj.testCaseApRunner).run(param).toTestResult,
          _.foldMap(obj.testCaseRunner).run(param))
        val duration = System.currentTimeMillis() - start
        obj.listener.onFinish(obj, name, r, log)
        r match {
          case TestResult.Done(results) => results match {
            case NonEmptyList(\/-(value), INil()) =>
              tracer.success()
              doc(value, event(Status.Success, duration, r))
            case NonEmptyList(-\/(Skipped(_)), INil()) =>
              tracer.ignore()
              event(Status.Ignored, duration, r)
            case _ =>
              tracer.failure()
              event(Status.Failure, duration, r)
          }
          case TestResult.Error(_, _) =>
            tracer.error()
            event(Status.Error, duration, r)
        }
      } finally {
        tracer.total()
        executor.shutdown()
      }
      eventHandler.handle(r)
      (name, r)
    }
    obj.listener.onFinishAll(obj, results.toList, log)
    Array()
  }

  override def tags() = Array()
}

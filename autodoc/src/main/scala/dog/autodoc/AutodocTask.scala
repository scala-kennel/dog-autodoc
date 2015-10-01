package dog
package autodoc

import java.util.concurrent.ForkJoinPool
import sbt.testing._
import scalaz._

private[autodoc] class AutodocTask(
  args: Array[String],
  override val taskdef: TaskDef,
  override val testClassName: String,
  testClassLoader: ClassLoader,
  tracer: DogTracer) extends DogTask(args, taskdef, testClassName, testClassLoader, tracer) {

  override def taskDef() = taskdef

  override def execute(eventHandler: EventHandler, loggers: Array[Logger]) = {

    val log = DogRunner.logger(loggers)

    lazy val executorService: ForkJoinPool = DogTask.createForkJoinPool(log)

    val only = scalaz.std.list.toNel(
      args.dropWhile("--only" != _).drop(1).takeWhile(arg => !arg.startsWith("--")).toList
    )

    try {
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

        val param = obj.paramEndo compose Param.executorService(executorService)
        val start = System.currentTimeMillis()
        val r = try {
          obj.listener.onStart(obj, name, test, log)
          val r = test.run(param)
          val duration = System.currentTimeMillis() - start
          obj.listener.onFinish(obj, name, test, r, log)
          r match {
            case Done(results) => results.list match {
              case List(\/-(value)) =>
                tracer.success()
                doc(value, event(Status.Success, duration, r))
              case List(-\/(Skipped(_))) =>
                tracer.ignore()
                event(Status.Ignored, duration, r)
              case _ =>
                tracer.failure()
                event(Status.Failure, duration, r)
            }
            case Error(_, _) =>
              tracer.error()
              event(Status.Error, duration, r)
          }
        } finally {
          tracer.total()
        }
        eventHandler.handle(r)
        (name, r)
      }
      obj.listener.onFinishAll(obj, results.toList, log)
      Array()
    } finally {
      executorService.shutdown()
    }
  }

  override def tags() = Array()
}

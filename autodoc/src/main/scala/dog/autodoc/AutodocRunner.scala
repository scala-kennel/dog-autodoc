package dog
package autodoc

import sbt.testing._

object AutodocRunner {
  private[dog] def logger(loggers: Array[Logger]): Logger = new Logger {
    override def warn(msg: String): Unit =
      loggers.foreach(_.warn(msg))
    override def error(msg: String): Unit =
      loggers.foreach(_.error(msg))
    override def ansiCodesSupported(): Boolean =
      loggers.forall(_.ansiCodesSupported())
    override def debug(msg: String): Unit =
      loggers.foreach(_.debug(msg))
    override def trace(t: Throwable): Unit =
      loggers.foreach(_.trace(t))
    override def info(msg: String): Unit =
      loggers.foreach(_.info(msg))
  }
}

final class AutodocRunner(
  override val args: Array[String],
  override val remoteArgs: Array[String],
  testClassLoader: ClassLoader
) extends Runner {

  val tracer = new DogTracer()

  private[this] val taskdef2task: TaskDef => Task = { taskdef =>
    val testClassName = taskdef.fullyQualifiedName()
    new AutodocTask(args, taskdef, testClassName, testClassLoader, tracer)
  }

  override def tasks(taskDefs: Array[TaskDef]) = taskDefs.map(taskdef2task)

  override def done() = tracer.done
}

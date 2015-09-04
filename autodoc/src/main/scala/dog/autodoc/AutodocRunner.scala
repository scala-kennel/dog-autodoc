package dog
package autodoc

import sbt.testing._

object AutodocRunner {
  private[autodoc] def taskdef2task(args: Array[String], remoteArgs: Array[String], loader: ClassLoader, tracer: DogTracer): TaskDef => Task = { taskdef =>
    val testClassName = taskdef.fullyQualifiedName()
    new AutodocTask(args, taskdef, testClassName, loader, tracer)
  }
}

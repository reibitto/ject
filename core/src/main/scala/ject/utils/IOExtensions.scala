package ject.utils

import zio.Task

import java.nio.file.{ Files, Path }

object IOExtensions {
  implicit class PathExtension(val self: Path) extends AnyVal {
    def ensureDirectoryExists(): Task[Unit] = Task {
      Option(self.getParent).foreach { directory =>
        Files.createDirectories(directory)
      }
    }
  }
}

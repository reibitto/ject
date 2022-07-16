package ject.utils

import zio.{Task, ZIO}

import java.nio.file.Files
import java.nio.file.Path

object IOExtensions {

  implicit class PathExtension(val self: Path) extends AnyVal {

    def ensureDirectoryExists(): Task[Unit] = ZIO.attempt {
      Option(self.getParent).foreach { directory =>
        Files.createDirectories(directory)
      }
    }
  }
}

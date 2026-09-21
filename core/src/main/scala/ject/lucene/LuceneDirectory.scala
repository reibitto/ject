package ject.lucene

import org.apache.lucene.store.{ByteBuffersDirectory, Directory, MMapDirectory}
import zio.*

import java.nio.file.Path

object LuceneDirectory {

  /** A memory-mapped directory backed by files at `path` on disk. */
  def fromPath(path: Path): ZIO[Scope, Throwable, Directory] =
    ZIO.fromAutoCloseable(ZIO.attempt(new MMapDirectory(path)))

  /** An in-memory Lucene directory, useful for tests that want to avoid
    * touching disk. The returned value itself holds all the index state, so a
    * writer and a reader that need to see each other's data must share the same
    * `Directory`. Each call creates an independent, empty one.
    */
  def inMemory: ZIO[Scope, Throwable, Directory] =
    ZIO.fromAutoCloseable(ZIO.attempt(new ByteBuffersDirectory()))
}

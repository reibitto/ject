package ject.lucene

import org.apache.lucene.store.{ByteBuffersDirectory, Directory, MMapDirectory}
import zio.*

import java.nio.file.Path

object LuceneDirectory {

  /** A memory-mapped directory backed by files at `path` on disk. */
  def fromPath(path: Path): ZIO[Scope, Throwable, Directory] =
    ZIO.fromAutoCloseable(ZIO.attempt(new MMapDirectory(path)))

  /** An in-memory Lucene directory (`ByteBuffersDirectory`), useful for tests
    * that want to avoid touching disk. Unlike a path-based directory, there's
    * no file-system location backing this that a second call could reopen — the
    * returned `Directory` value itself holds all the index state. A writer and
    * a reader that need to see each other's data must be given this *same*
    * `Directory` value; each call to `inMemory` creates an independent, empty
    * one.
    */
  def inMemory: ZIO[Scope, Throwable, Directory] =
    ZIO.fromAutoCloseable(ZIO.attempt(new ByteBuffersDirectory()))
}

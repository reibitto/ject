package ject.ko.lucene

import ject.ko.docs.WordDoc
import ject.lucene.{DocEncoder, DocWriter, LuceneDirectory}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.Directory
import zio.*

import java.nio.file.Path

final case class WordWriter(writer: IndexWriter, docEncoder: DocEncoder[WordDoc]) extends DocWriter[WordDoc]

object WordWriter {

  /** Builds a writer on top of an already-acquired `Directory` (e.g. a
    * `ByteBuffersDirectory` shared with a reader via `LuceneDirectory.inMemory`
    * — the directory itself is not closed when this writer's scope ends, since
    * it may still be in use elsewhere).
    */
  def make(directory: Directory, autoCommitOnRelease: Boolean): RIO[Scope, WordWriter] =
    for {
      config <- ZIO.succeed(new IndexWriterConfig(WordDoc.docDecoder.analyzer))
      writer <- ZIO.acquireRelease(ZIO.attempt(new IndexWriter(directory, config))) { writer =>
                  ZIO.attemptBlocking {
                    if (autoCommitOnRelease) {
                      writer.commit()
                    }

                    writer.close()
                  }.orDie
                }
    } yield WordWriter(writer, WordDoc.docEncoder)

  def make(directory: Directory): RIO[Scope, WordWriter] =
    make(directory, autoCommitOnRelease = true)

  /** Builds a writer backed by files at `directory` on disk, owning the
    * underlying `MMapDirectory`'s lifecycle (closed when this writer's scope
    * ends).
    */
  def make(directory: Path, autoCommitOnRelease: Boolean = true): RIO[Scope, WordWriter] =
    for {
      dir    <- LuceneDirectory.fromPath(directory)
      writer <- make(dir, autoCommitOnRelease)
    } yield writer
}

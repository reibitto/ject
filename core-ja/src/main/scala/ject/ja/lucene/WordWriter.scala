package ject.ja.lucene

import ject.ja.docs.WordDoc
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
  def make(
      directory: Directory,
      encoder: DocEncoder[WordDoc],
      autoCommitOnRelease: Boolean
  ): ZIO[Scope, Throwable, WordWriter] =
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
    } yield WordWriter(writer, encoder)

  def make(directory: Directory, encoder: DocEncoder[WordDoc]): ZIO[Scope, Throwable, WordWriter] =
    make(directory, encoder, autoCommitOnRelease = true)

  def make(directory: Directory): ZIO[Scope, Throwable, WordWriter] =
    make(directory, WordDoc.docEncoder(includeInflections = true), autoCommitOnRelease = true)

  /** Builds a writer backed by files at `directory` on disk, owning the
    * underlying `MMapDirectory`'s lifecycle (closed when this writer's scope
    * ends).
    */
  def make(
      directory: Path,
      encoder: DocEncoder[WordDoc] = WordDoc.docEncoder(includeInflections = true),
      autoCommitOnRelease: Boolean = true
  ): ZIO[Scope, Throwable, WordWriter] =
    for {
      dir    <- LuceneDirectory.fromPath(directory)
      writer <- make(dir, encoder, autoCommitOnRelease)
    } yield writer
}

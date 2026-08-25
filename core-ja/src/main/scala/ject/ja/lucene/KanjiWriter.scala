package ject.ja.lucene

import ject.ja.docs.KanjiDoc
import ject.lucene.{DocEncoder, DocWriter, LuceneDirectory}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.Directory
import zio.*

import java.nio.file.Path

final case class KanjiWriter(writer: IndexWriter, docEncoder: DocEncoder[KanjiDoc]) extends DocWriter[KanjiDoc]

object KanjiWriter {

  /** Builds a writer on top of an already-acquired `Directory` (e.g. a
    * `ByteBuffersDirectory` shared with a reader via `LuceneDirectory.inMemory`
    * — the directory itself is not closed when this writer's scope ends, since
    * it may still be in use elsewhere).
    */
  def make(
      directory: Directory,
      encoder: DocEncoder[KanjiDoc],
      autoCommitOnRelease: Boolean
  ): ZIO[Scope, Throwable, KanjiWriter] =
    for {
      config <- ZIO.succeed(new IndexWriterConfig(KanjiDoc.docDecoder.analyzer))
      writer <- ZIO.acquireRelease(ZIO.attempt(new IndexWriter(directory, config))) { writer =>
                  ZIO.attemptBlocking {
                    if (autoCommitOnRelease) {
                      writer.commit()
                    }

                    writer.close()
                  }.orDie
                }
    } yield KanjiWriter(writer, encoder)

  def make(directory: Directory, encoder: DocEncoder[KanjiDoc]): ZIO[Scope, Throwable, KanjiWriter] =
    make(directory, encoder, autoCommitOnRelease = true)

  def make(directory: Directory): ZIO[Scope, Throwable, KanjiWriter] =
    make(directory, KanjiDoc.docEncoder, autoCommitOnRelease = true)

  /** Builds a writer backed by files at `directory` on disk, owning the
    * underlying `MMapDirectory`'s lifecycle (closed when this writer's scope
    * ends).
    */
  def make(
      directory: Path,
      encoder: DocEncoder[KanjiDoc] = KanjiDoc.docEncoder,
      autoCommitOnRelease: Boolean = true
  ): ZIO[Scope, Throwable, KanjiWriter] =
    for {
      dir    <- LuceneDirectory.fromPath(directory)
      writer <- make(dir, encoder, autoCommitOnRelease)
    } yield writer
}

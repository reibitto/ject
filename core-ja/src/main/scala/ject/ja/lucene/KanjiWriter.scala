package ject.ja.lucene

import ject.ja.docs.KanjiDoc
import ject.lucene.{DocEncoder, DocWriter}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory
import zio.*

import java.nio.file.Path

final case class KanjiWriter(writer: IndexWriter, docEncoder: DocEncoder[KanjiDoc]) extends DocWriter[KanjiDoc]

object KanjiWriter {

  def make(
      directory: Path,
      encoder: DocEncoder[KanjiDoc] = KanjiDoc.docEncoder,
      autoCommitOnRelease: Boolean = true
  ): ZIO[Scope, Throwable, KanjiWriter] =
    for {
      index <- ZIO.fromAutoCloseable(ZIO.attempt(new MMapDirectory(directory)))
      config = new IndexWriterConfig(KanjiDoc.docDecoder.analyzer)
      writer <- ZIO.acquireRelease(ZIO.attempt(new IndexWriter(index, config))) { writer =>
                  ZIO.attemptBlocking {
                    if (autoCommitOnRelease) {
                      writer.commit()
                    }

                    writer.close()
                  }.orDie
                }
    } yield KanjiWriter(writer, encoder)
}

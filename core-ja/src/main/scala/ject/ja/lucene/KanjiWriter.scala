package ject.ja.lucene

import ject.ja.docs.KanjiDoc
import ject.lucene.{DocEncoder, DocWriter}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory
import zio.{Scope, ZIO}

import java.nio.file.Path

final case class KanjiWriter(writer: IndexWriter, docEncoder: DocEncoder[KanjiDoc]) extends DocWriter[KanjiDoc]

object KanjiWriter {

  def make(
      directory: Path,
      encoder: DocEncoder[KanjiDoc] = KanjiDoc.docEncoder,
      autoCommitOnRelease: Boolean = true
  ): ZIO[Scope, Throwable, KanjiWriter] =
    (for {
      config <- ZIO.attempt(new IndexWriterConfig(KanjiDoc.docDecoder.analyzer))
      index  <- ZIO.attempt(new MMapDirectory(directory))
      writer <- ZIO.attempt(new IndexWriter(index, config))
    } yield KanjiWriter(writer, encoder)).withFinalizer { writer =>
      ZIO.attempt {
        if (autoCommitOnRelease) {
          writer.writer.commit()
        }

        writer.writer.close()
      }.orDie
    }
}

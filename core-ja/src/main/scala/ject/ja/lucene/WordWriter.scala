package ject.ja.lucene

import ject.ja.docs.WordDoc
import ject.lucene.{DocEncoder, DocWriter}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory
import zio.{Scope, ZIO}

import java.nio.file.Path

final case class WordWriter(writer: IndexWriter, docEncoder: DocEncoder[WordDoc]) extends DocWriter[WordDoc]

object WordWriter {

  def make(
      directory: Path,
      encoder: DocEncoder[WordDoc] = WordDoc.docEncoder(includeInflections = true),
      autoCommitOnRelease: Boolean = true
  ): ZIO[Scope, Throwable, WordWriter] =
    (for {
      config <- ZIO.attempt(new IndexWriterConfig(WordDoc.docDecoder.analyzer))
      index  <- ZIO.attempt(new MMapDirectory(directory))
      writer <- ZIO.attempt(new IndexWriter(index, config))
    } yield WordWriter(writer, encoder)).withFinalizer { writer =>
      ZIO.attempt {
        if (autoCommitOnRelease) {
          writer.writer.commit()
        }

        writer.writer.close()
      }.orDie
    }
}

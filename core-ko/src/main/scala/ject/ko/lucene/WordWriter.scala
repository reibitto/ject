package ject.ko.lucene

import ject.ko.docs.WordDoc
import ject.lucene.{DocEncoder, DocWriter}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory
import zio.*

import java.nio.file.Path

final case class WordWriter(writer: IndexWriter, docEncoder: DocEncoder[WordDoc]) extends DocWriter[WordDoc]

object WordWriter {

  def make(directory: Path, autoCommitOnRelease: Boolean = true): RIO[Scope, WordWriter] =
    (for {
      config <- ZIO.attempt(new IndexWriterConfig(WordDoc.docDecoder.analyzer))
      index  <- ZIO.attempt(new MMapDirectory(directory))
      writer <- ZIO.attempt(new IndexWriter(index, config))
    } yield WordWriter(writer, WordDoc.docEncoder)).withFinalizer { writer =>
      ZIO.attempt {
        if (autoCommitOnRelease) {
          writer.writer.commit()
        }

        writer.writer.close()
      }.orDie
    }
}

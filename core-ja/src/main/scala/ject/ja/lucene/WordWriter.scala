package ject.ja.lucene

import ject.ja.docs.WordDoc
import ject.lucene.DocWriter
import ject.lucene.DocEncoder
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.MMapDirectory
import zio.Task
import zio.TaskManaged

import java.nio.file.Path

final case class WordWriter(writer: IndexWriter, docEncoder: DocEncoder[WordDoc]) extends DocWriter[WordDoc]

object WordWriter {
  def make(
    directory: Path,
    encoder: DocEncoder[WordDoc] = WordDoc.docEncoder(includeInflections = true),
    autoCommitOnRelease: Boolean = true
  ): TaskManaged[WordWriter] =
    (for {
      config <- Task(new IndexWriterConfig(WordDoc.docDecoder.analyzer))
      index  <- Task(new MMapDirectory(directory))
      writer <- Task(new IndexWriter(index, config))
    } yield WordWriter(writer, encoder)).toManaged { writer =>
      Task {
        if (autoCommitOnRelease) {
          writer.writer.commit()
        }

        writer.writer.close()
      }.orDie
    }
}

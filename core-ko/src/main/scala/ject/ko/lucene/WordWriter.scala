package ject.ko.lucene

import ject.ko.docs.WordDoc
import ject.lucene.DocEncoder
import ject.lucene.DocWriter
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.MMapDirectory
import zio.Task
import zio.TaskManaged

import java.nio.file.Path

final case class WordWriter(writer: IndexWriter, docEncoder: DocEncoder[WordDoc]) extends DocWriter[WordDoc]

object WordWriter {
  def make(directory: Path, autoCommitOnRelease: Boolean = true): TaskManaged[WordWriter] =
    (for {
      config <- Task(new IndexWriterConfig(WordDoc.docDecoder.analyzer))
      index  <- Task(new MMapDirectory(directory))
      writer <- Task(new IndexWriter(index, config))
    } yield WordWriter(writer, WordDoc.docEncoder)).toManaged { writer =>
      Task {
        if (autoCommitOnRelease) {
          writer.writer.commit()
        }

        writer.writer.close()
      }.orDie
    }
}

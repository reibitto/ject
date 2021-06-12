package ject.lucene

import ject.docs.KanjiDoc
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.MMapDirectory
import zio.Task
import zio.TaskManaged

import java.nio.file.Path

final case class KanjiWriter(writer: IndexWriter) extends DocWriter[KanjiDoc]

object KanjiWriter {
  def make(directory: Path, autoCommitOnRelease: Boolean = true): TaskManaged[KanjiWriter] =
    (for {
      config <- Task(new IndexWriterConfig(KanjiDoc.documentDecoder.analyzer))
      index  <- Task(new MMapDirectory(directory))
      writer <- Task(new IndexWriter(index, config))
    } yield KanjiWriter(writer)).toManaged { writer =>
      Task {
        if (autoCommitOnRelease) {
          writer.writer.commit()
        }

        writer.writer.close()
      }.orDie
    }
}

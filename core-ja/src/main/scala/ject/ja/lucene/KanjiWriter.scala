package ject.ja.lucene

import ject.ja.docs.KanjiDoc
import ject.lucene.DocEncoder
import ject.lucene.DocWriter
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.MMapDirectory
import zio.Task
import zio.TaskManaged

import java.nio.file.Path

final case class KanjiWriter(writer: IndexWriter, docEncoder: DocEncoder[KanjiDoc]) extends DocWriter[KanjiDoc]

object KanjiWriter {
  def make(
    directory: Path,
    encoder: DocEncoder[KanjiDoc] = KanjiDoc.docEncoder,
    autoCommitOnRelease: Boolean = true
  ): TaskManaged[KanjiWriter] =
    (for {
      config <- Task(new IndexWriterConfig(KanjiDoc.docDecoder.analyzer))
      index  <- Task(new MMapDirectory(directory))
      writer <- Task(new IndexWriter(index, config))
    } yield KanjiWriter(writer, encoder)).toManaged { writer =>
      Task {
        if (autoCommitOnRelease) {
          writer.writer.commit()
        }

        writer.writer.close()
      }.orDie
    }
}

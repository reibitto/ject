package ject.ja.lucene

import ject.ja.docs.WordDoc
import ject.lucene.{DocEncoder, DocWriter}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory
import zio.*

import java.nio.file.Path

final case class WordWriter(writer: IndexWriter, docEncoder: DocEncoder[WordDoc]) extends DocWriter[WordDoc]

object WordWriter {

  def make(
      directory: Path,
      encoder: DocEncoder[WordDoc] = WordDoc.docEncoder(includeInflections = true),
      autoCommitOnRelease: Boolean = true
  ): ZIO[Scope, Throwable, WordWriter] =
    for {
      index <- ZIO.fromAutoCloseable(ZIO.attempt(new MMapDirectory(directory)))
      config = new IndexWriterConfig(WordDoc.docDecoder.analyzer)
      writer <- ZIO.acquireRelease(ZIO.attempt(new IndexWriter(index, config))) { writer =>
                  ZIO.attemptBlocking {
                    if (autoCommitOnRelease) {
                      writer.commit()
                    }

                    writer.close()
                  }.orDie
                }
    } yield WordWriter(writer, encoder)
}

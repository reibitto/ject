package ject.lucene

import org.apache.lucene.index.IndexWriter
import zio.{Task, ZIO}

trait DocWriter[A] {
  def writer: IndexWriter

  def docEncoder: DocEncoder[A]

  def add(doc: A): Task[Long] = ZIO.attempt(writer.addDocument(docEncoder.encode(doc)))

  def addBulk(docs: A*): Task[Long] = {
    import scala.jdk.CollectionConverters.*

    ZIO.attempt {
      writer.addDocuments(docs.map(docEncoder.encode).asJava)
    }
  }
}

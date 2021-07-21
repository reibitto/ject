package ject.lucene

import org.apache.lucene.index.IndexWriter
import zio.Task

trait DocWriter[A] {
  def writer: IndexWriter

  def docEncoder: DocEncoder[A]

  def add(doc: A): Task[Long] = Task(writer.addDocument(docEncoder.encode(doc)))

  def addBulk(docs: A*): Task[Long] = {
    import scala.jdk.CollectionConverters._

    Task {
      writer.addDocuments(docs.map(docEncoder.encode).asJava)
    }
  }
}

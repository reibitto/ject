package ject.lucene

import ject.docs.Doc
import org.apache.lucene.index.IndexWriter
import zio.Task

trait DocWriter[A <: Doc] {
  def writer: IndexWriter

  def add(doc: A): Task[Long] = Task(writer.addDocument(doc.toLucene))

  def addBulk(docs: A*): Task[Long] = {
    import scala.jdk.CollectionConverters._

    Task {
      writer.addDocuments(docs.map(_.toLucene).asJava)
    }
  }
}

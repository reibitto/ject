package ject.lucene

import org.apache.lucene.index.IndexWriter
import zio.*

trait DocWriter[A] {
  def writer: IndexWriter

  def docEncoder: DocEncoder[A]

  def add(doc: A): Task[Unit] =
    for {
      document <- docEncoder.encode(doc)
      _        <- ZIO.attempt(writer.addDocument(document))
    } yield ()

  def addBulk(docs: A*): Task[Unit] = {
    import scala.jdk.CollectionConverters.*

    for {
      documents <- ZIO.foreach(docs) { doc =>
                     docEncoder.encode(doc)
                   }
      _ <- ZIO.attempt {
             writer.addDocuments(documents.asJava)
           }
    } yield ()
  }
}

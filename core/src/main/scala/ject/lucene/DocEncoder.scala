package ject.lucene

import org.apache.lucene.document.Document
import zio.Task

trait DocEncoder[A] {
  def encode(a: A): Task[Document]
}

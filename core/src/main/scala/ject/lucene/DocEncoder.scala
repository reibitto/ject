package ject.lucene

import org.apache.lucene.document.Document
import zio.*

trait DocEncoder[A] {
  def encode(a: A): Task[Document]
}

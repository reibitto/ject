package ject.lucene

import org.apache.lucene.document.Document

trait DocEncoder[A] {
  def encode(a: A): Document
}

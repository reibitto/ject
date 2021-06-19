package ject.docs

import org.apache.lucene.document.Document

trait Doc {
  def toLucene: Document
}

package ject.lucene

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document

trait DocumentDecoder[A] {
  def analyzer: Analyzer
  def decode(document: Document): A
}

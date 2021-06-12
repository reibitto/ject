package ject.lucene

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document

trait DocDecoder[A] {
  def analyzer: Analyzer
  def decode(document: Document): A
}

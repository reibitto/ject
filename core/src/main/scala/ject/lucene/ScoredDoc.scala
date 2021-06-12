package ject.lucene

final case class ScoredDoc[A](doc: A, score: Double)

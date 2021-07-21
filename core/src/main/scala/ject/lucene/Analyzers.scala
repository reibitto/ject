package ject.lucene

import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer

object Analyzers {
  lazy val standard: StandardAnalyzer = new StandardAnalyzer()
  lazy val english: EnglishAnalyzer   = new EnglishAnalyzer()
}

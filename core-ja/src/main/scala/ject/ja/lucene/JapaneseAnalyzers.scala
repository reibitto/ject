package ject.ja.lucene

import org.apache.lucene.analysis.ja.JapaneseAnalyzer

object JapaneseAnalyzers {
  lazy val japanese: JapaneseAnalyzer = new JapaneseAnalyzer()
}

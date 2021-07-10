package ject.ko.lucene

import org.apache.lucene.analysis.ko.KoreanAnalyzer

object KoreanAnalyzers {
  lazy val korean: KoreanAnalyzer = new KoreanAnalyzer()
}

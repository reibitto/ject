package ject.ja.lucene.field

import enumeratum._
import ject.lucene.Analyzers
import ject.lucene.field.LuceneField
import org.apache.lucene.analysis.Analyzer

sealed abstract class KanjiField(val analyzer: Analyzer) extends LuceneField

object KanjiField extends Enum[KanjiField] {
  case object Kanji         extends KanjiField(Analyzers.standard)
  case object Meaning       extends KanjiField(Analyzers.english)
  case object OnYomi        extends KanjiField(Analyzers.standard)
  case object KunYomi       extends KanjiField(Analyzers.standard)
  case object Nanori        extends KanjiField(Analyzers.standard)
  case object KoreanReading extends KanjiField(Analyzers.standard)
  case object RadicalId     extends KanjiField(Analyzers.standard)
  case object Parts         extends KanjiField(Analyzers.standard)
  case object StrokeCount   extends KanjiField(Analyzers.standard)
  case object Frequency     extends KanjiField(Analyzers.standard)
  case object Jlpt          extends KanjiField(Analyzers.standard)
  case object Grade         extends KanjiField(Analyzers.standard)

  val values: IndexedSeq[KanjiField] = findValues
}

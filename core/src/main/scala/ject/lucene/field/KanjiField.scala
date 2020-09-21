package ject.lucene.field

import enumeratum._

sealed trait KanjiField extends LuceneField

object KanjiField extends Enum[KanjiField] {
  case object Kanji       extends KanjiField
  case object Meaning     extends KanjiField
  case object OnYomi      extends KanjiField
  case object KunYomi     extends KanjiField
  case object Nanori      extends KanjiField
  case object RadicalId   extends KanjiField
  case object Parts       extends KanjiField
  case object StrokeCount extends KanjiField
  case object Frequency   extends KanjiField
  case object Jlpt        extends KanjiField
  case object Grade       extends KanjiField

  lazy val values: IndexedSeq[KanjiField] = findValues
}

package ject.entity

final case class KanjiDocument(
  kanji: String,
  meaning: Seq[String],
  onYomi: Seq[String],
  kunYomi: Seq[String],
  nanori: Seq[String],
  radicalId: Int,
  parts: Seq[String],
  strokeCount: Seq[Int],
  frequency: Option[Int],
  jlpt: Option[Int],
  grade: Option[Int]
)

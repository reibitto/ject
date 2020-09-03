package ject.lucene.schema

import enumeratum._
import ject.entity.KanjiDocument
import org.apache.lucene.document.Document

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

object KanjiSchema {
  def from(document: Document): KanjiDocument =
    KanjiDocument(
      document.get(KanjiField.Kanji.entryName),
      document.getValues(KanjiField.Meaning.entryName).toIndexedSeq,
      document.getValues(KanjiField.OnYomi.entryName).toIndexedSeq,
      document.getValues(KanjiField.KunYomi.entryName).toIndexedSeq,
      document.getValues(KanjiField.Nanori.entryName).toIndexedSeq,
      document.get(KanjiField.RadicalId.entryName).toInt,
      document.getValues(KanjiField.Parts.entryName).toIndexedSeq,
      document.getValues(KanjiField.StrokeCount.entryName).map(_.toInt).toIndexedSeq,
      Option(document.get(KanjiField.Frequency.entryName)).map(_.toInt),
      Option(document.get(KanjiField.Jlpt.entryName)).map(_.toInt),
      Option(document.get(KanjiField.Grade.entryName)).map(_.toInt)
    )
}

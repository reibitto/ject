package ject.entity

import ject.lucene.DocumentDecoder
import ject.lucene.field.KanjiField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document

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

object KanjiDocument {
  implicit val documentDecoder: DocumentDecoder[KanjiDocument] = new DocumentDecoder[KanjiDocument] {
    val analyzer: Analyzer = new StandardAnalyzer

    def decode(document: Document): KanjiDocument =
      KanjiDocument(
        document.get(KanjiField.Kanji.entryName),
        document.getValues(KanjiField.Meaning.entryName).toIndexedSeq,
        document.getValues(KanjiField.OnYomi.entryName).toIndexedSeq,
        document.getValues(KanjiField.KunYomi.entryName).toIndexedSeq,
        document.getValues(KanjiField.Nanori.entryName).toIndexedSeq,
        document.get(KanjiField.RadicalId.entryName).toInt,
        document.getValues(KanjiField.Parts.entryName).toIndexedSeq,
        document.getValues(KanjiField.StrokeCount.entryName).toIndexedSeq.map(_.toInt),
        Option(document.get(KanjiField.Frequency.entryName)).map(_.toInt),
        Option(document.get(KanjiField.Jlpt.entryName)).map(_.toInt),
        Option(document.get(KanjiField.Grade.entryName)).map(_.toInt)
      )
  }
}

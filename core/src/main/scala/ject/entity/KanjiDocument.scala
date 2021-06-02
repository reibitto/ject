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
        kanji = document.get(KanjiField.Kanji.entryName),
        meaning = document.getValues(KanjiField.Meaning.entryName).toIndexedSeq,
        onYomi = document.getValues(KanjiField.OnYomi.entryName).toIndexedSeq,
        kunYomi = document.getValues(KanjiField.KunYomi.entryName).toIndexedSeq,
        nanori = document.getValues(KanjiField.Nanori.entryName).toIndexedSeq,
        radicalId = document.get(KanjiField.RadicalId.entryName).toInt,
        parts = document.getValues(KanjiField.Parts.entryName).toIndexedSeq,
        strokeCount = document.getValues(KanjiField.StrokeCount.entryName).toIndexedSeq.map(_.toInt),
        frequency = Option(document.get(KanjiField.Frequency.entryName)).map(_.toInt),
        jlpt = Option(document.get(KanjiField.Jlpt.entryName)).map(_.toInt),
        grade = Option(document.get(KanjiField.Grade.entryName)).map(_.toInt)
      )
  }
}

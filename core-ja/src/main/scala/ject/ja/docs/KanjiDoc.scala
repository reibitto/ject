package ject.ja.docs

import ject.docs.Doc
import ject.ja.lucene.field.KanjiField
import ject.lucene.DocDecoder
import ject.lucene.field.LuceneField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document._

final case class KanjiDoc(
  kanji: String,
  meaning: Seq[String],
  onYomi: Seq[String],
  kunYomi: Seq[String],
  nanori: Seq[String],
  koreanReadings: Seq[String],
  radicalId: Int,
  parts: Seq[String],
  strokeCount: Seq[Int],
  frequency: Option[Int],
  jlpt: Option[Int],
  grade: Option[Int]
) extends Doc {
  def isJouyouKanji: Boolean = grade.exists(_ <= 8)

  def toLucene: Document = {
    val doc = new Document()

    doc.add(new StringField(KanjiField.Kanji.entryName, kanji, Field.Store.YES))

    meaning.foreach { value =>
      doc.add(new StringField(KanjiField.Meaning.entryName, value, Field.Store.YES))
    }

    onYomi.foreach { value =>
      doc.add(new StringField(KanjiField.OnYomi.entryName, value, Field.Store.YES))
    }

    kunYomi.foreach { value =>
      doc.add(new TextField(KanjiField.KunYomi.entryName, value, Field.Store.YES))
    }

    nanori.foreach { value =>
      doc.add(new StringField(KanjiField.Nanori.entryName, value, Field.Store.YES))
    }

    koreanReadings.foreach { value =>
      doc.add(new StringField(KanjiField.KoreanReading.entryName, value, Field.Store.YES))
    }

    doc.add(new IntPoint(KanjiField.RadicalId.entryName, radicalId))
    doc.add(new StoredField(KanjiField.RadicalId.entryName, radicalId))

    parts.foreach { value =>
      doc.add(new StringField(KanjiField.Parts.entryName, value, Field.Store.YES))
    }

    strokeCount.foreach { value =>
      doc.add(new IntPoint(KanjiField.StrokeCount.entryName, value))
      doc.add(new StoredField(KanjiField.StrokeCount.entryName, value))
    }

    frequency.foreach { value =>
      doc.add(new IntPoint(KanjiField.Frequency.entryName, value))
      doc.add(new StoredField(KanjiField.Frequency.entryName, value))
    }

    doc.add(new SortedNumericDocValuesField(KanjiField.Frequency.entryName, frequency.getOrElse(Int.MaxValue).toLong))

    jlpt.foreach { value =>
      doc.add(new IntPoint(KanjiField.Jlpt.entryName, value))
      doc.add(new StoredField(KanjiField.Jlpt.entryName, value))
    }

    grade.foreach { value =>
      doc.add(new IntPoint(KanjiField.Grade.entryName, value))
      doc.add(new StoredField(KanjiField.Grade.entryName, value))
    }

    doc.add(new SortedNumericDocValuesField(KanjiField.Grade.entryName, grade.getOrElse(Int.MaxValue).toLong))

    doc
  }
}

object KanjiDoc {
  implicit val documentDecoder: DocDecoder[KanjiDoc] = new DocDecoder[KanjiDoc] {
    val analyzer: Analyzer = LuceneField.perFieldAnalyzer(KanjiField.values)

    def decode(document: Document): KanjiDoc =
      KanjiDoc(
        kanji = document.get(KanjiField.Kanji.entryName),
        meaning = document.getValues(KanjiField.Meaning.entryName).toIndexedSeq,
        onYomi = document.getValues(KanjiField.OnYomi.entryName).toIndexedSeq,
        kunYomi = document.getValues(KanjiField.KunYomi.entryName).toIndexedSeq,
        nanori = document.getValues(KanjiField.Nanori.entryName).toIndexedSeq,
        koreanReadings = document.getValues(KanjiField.KoreanReading.entryName).toIndexedSeq,
        radicalId = document.get(KanjiField.RadicalId.entryName).toInt,
        parts = document.getValues(KanjiField.Parts.entryName).toIndexedSeq,
        strokeCount = document.getValues(KanjiField.StrokeCount.entryName).toIndexedSeq.map(_.toInt),
        frequency = Option(document.get(KanjiField.Frequency.entryName)).map(_.toInt),
        jlpt = Option(document.get(KanjiField.Jlpt.entryName)).map(_.toInt),
        grade = Option(document.get(KanjiField.Grade.entryName)).map(_.toInt)
      )
  }
}

package ject.lucene

import java.nio.file.Path

import ject.entity.{ KanjiDocument, WordDocument }
import ject.lucene.schema.KanjiField
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriter
import zio.Task

class KanjiIndex(directory: Path) extends LuceneIndex[WordDocument](directory) {
  def add(entry: KanjiDocument, writer: IndexWriter): Task[Unit] =
    Task {
      val doc = new Document()

      doc.add(new StringField(KanjiField.Kanji.entryName, entry.kanji, Field.Store.YES))

      doc.add(new LongPoint(KanjiField.RadicalId.entryName, entry.radicalId))

      entry.meaning.foreach { value =>
        doc.add(new StringField(KanjiField.Meaning.entryName, value, Field.Store.YES))
      }

      entry.onYomi.foreach { value =>
        doc.add(new StringField(KanjiField.OnYomi.entryName, value, Field.Store.YES))
      }

      entry.kunYomi.foreach { value =>
        doc.add(new TextField(KanjiField.KunYomi.entryName, value, Field.Store.YES))
      }

      entry.nanori.foreach { value =>
        doc.add(new StringField(KanjiField.Nanori.entryName, value, Field.Store.YES))
      }

      doc.add(new LongPoint(KanjiField.RadicalId.entryName, entry.radicalId))

      entry.parts.foreach { value =>
        doc.add(new StringField(KanjiField.Parts.entryName, value, Field.Store.YES))
      }

      entry.strokeCount.foreach { value =>
        doc.add(new LongPoint(KanjiField.StrokeCount.entryName, value))
      }

      entry.frequency.foreach { value =>
        doc.add(new LongPoint(KanjiField.Frequency.entryName, value))
      }

      entry.jlpt.foreach { value =>
        doc.add(new LongPoint(KanjiField.Jlpt.entryName, value))
      }

      entry.grade.foreach { value =>
        doc.add(new LongPoint(KanjiField.Grade.entryName, value))
      }

      writer.addDocument(doc)
      ()
    }
}

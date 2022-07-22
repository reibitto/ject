package ject.ja.docs

import ject.ja.lucene.field.WordField
import ject.ja.text.Inflection
import ject.ja.text.WordType
import ject.lucene.field.LuceneField
import ject.lucene.DocDecoder
import ject.lucene.DocEncoder
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField

final case class WordDoc(
  id: String,
  kanjiTerms: Seq[String],
  readingTerms: Seq[String],
  definitions: Seq[String],
  tags: Seq[String],
  partsOfSpeech: Seq[String]
) {

  def render: String = {
    val terms = (kanjiTerms ++ readingTerms).mkString(" ")
    s"$terms: ${definitions.mkString("; ")}"
  }
}

object WordDoc {

  implicit val docDecoder: DocDecoder[WordDoc] = new DocDecoder[WordDoc] {
    val analyzer: Analyzer = LuceneField.perFieldAnalyzer(WordField.values)

    def decode(document: Document): WordDoc =
      WordDoc(
        id = document.get(WordField.Id.entryName),
        kanjiTerms = document.getValues(WordField.KanjiTerm.entryName).toIndexedSeq,
        readingTerms = document.getValues(WordField.ReadingTerm.entryName).toIndexedSeq,
        definitions = document.getValues(WordField.Definition.entryName).toIndexedSeq,
        tags = document.getValues(WordField.Tags.entryName).toIndexedSeq,
        partsOfSpeech = document.getValues(WordField.PartOfSpeech.entryName).toIndexedSeq
      )
  }

  def docEncoder(includeInflections: Boolean): DocEncoder[WordDoc] = (a: WordDoc) => {
    val doc = new Document()

    doc.add(new StringField(WordField.Id.entryName, a.id, Field.Store.YES))

    a.kanjiTerms.foreach { value =>
      doc.add(new StringField(WordField.KanjiTerm.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.KanjiTermAnalyzed.entryName, value, Field.Store.NO))
    }

    a.readingTerms.foreach { value =>
      doc.add(new StringField(WordField.ReadingTerm.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.ReadingTermAnalyzed.entryName, value, Field.Store.NO))
    }

    a.definitions.foreach { value =>
      doc.add(new TextField(WordField.Definition.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.DefinitionOther.entryName, value, Field.Store.NO))
    }

    a.tags.foreach { value =>
      doc.add(new StringField(WordField.Tags.entryName, value, Field.Store.YES))
    }

    a.partsOfSpeech.foreach { value =>
      doc.add(new StringField(WordField.PartOfSpeech.entryName, value, Field.Store.YES))
    }

    if (includeInflections)
      indexInflections(a, doc)

    doc
  }

  private def indexInflections(a: WordDoc, doc: Document): Unit = {
    def indexTerms(terms: Seq[String], field: WordField, wordType: WordType): Unit =
      terms.foreach { value =>
        Inflection.inflectAll(value, wordType).foreach {
          case (_, Right(chunk)) =>
            chunk.foreach { s =>
              doc.add(new StringField(field.entryName, s, Field.Store.NO))
            }

          case _ => ()
        }
      }

    if (a.partsOfSpeech.contains("adj-i")) {
      indexTerms(a.kanjiTerms, WordField.KanjiTermInflected, WordType.AdjectiveI)
      indexTerms(a.readingTerms, WordField.ReadingTermInflected, WordType.AdjectiveI)
    } else if (a.partsOfSpeech.contains("v1")) {
      indexTerms(a.kanjiTerms, WordField.KanjiTermInflected, WordType.VerbIchidan)
      indexTerms(a.readingTerms, WordField.ReadingTermInflected, WordType.VerbIchidan)
    } else if (a.partsOfSpeech.exists(_.startsWith("v5k-s"))) {
      indexTerms(a.kanjiTerms, WordField.KanjiTermInflected, WordType.VerbIku)
      indexTerms(a.readingTerms, WordField.ReadingTermInflected, WordType.VerbIku)
    } else if (a.partsOfSpeech.exists(_.startsWith("v5"))) {
      indexTerms(a.kanjiTerms, WordField.KanjiTermInflected, WordType.VerbGodan)
      indexTerms(a.readingTerms, WordField.ReadingTermInflected, WordType.VerbGodan)
    }
  }
}

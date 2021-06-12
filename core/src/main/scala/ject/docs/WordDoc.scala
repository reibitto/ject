package ject.docs

import ject.lucene.DocDecoder
import ject.lucene.field.WordField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField

import scala.jdk.CollectionConverters._

final case class WordDoc(
  id: String,
  kanjiTerms: Seq[String] = Seq.empty,
  readingTerms: Seq[String] = Seq.empty,
  definitions: Seq[String] = Seq.empty,
  tags: Seq[String] = Seq.empty,
  partsOfSpeech: Seq[String] = Seq.empty
) extends Doc {
  def render: String = {
    val terms = (kanjiTerms ++ readingTerms).mkString(" ")
    s"$terms: ${definitions.mkString("; ")}"
  }

  def toLucene: Document = {
    val doc = new Document()

    doc.add(new StringField(WordField.Id.entryName, id, Field.Store.YES))

    kanjiTerms.foreach { value =>
      doc.add(new StringField(WordField.KanjiTerm.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.KanjiTermFuzzy.entryName, value, Field.Store.NO))
    }

    readingTerms.foreach { value =>
      doc.add(new StringField(WordField.ReadingTerm.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.ReadingTermFuzzy.entryName, value, Field.Store.NO))
    }

    definitions.foreach { value =>
      doc.add(new TextField(WordField.Definition.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.DefinitionOther.entryName, value, Field.Store.NO))
    }

    tags.foreach { value =>
      doc.add(new StringField(WordField.Tags.entryName, value, Field.Store.YES))
    }

    partsOfSpeech.foreach { value =>
      doc.add(new StringField(WordField.PartOfSpeech.entryName, value, Field.Store.YES))
    }

    doc
  }
}

object WordDoc {
  implicit val documentDecoder: DocDecoder[WordDoc] = new DocDecoder[WordDoc] {
    val analyzer: PerFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(
      new StandardAnalyzer,
      Map[String, Analyzer](
        WordField.KanjiTerm.entryName        -> new JapaneseAnalyzer,
        WordField.KanjiTermFuzzy.entryName   -> new JapaneseAnalyzer,
        WordField.ReadingTerm.entryName      -> new JapaneseAnalyzer,
        WordField.ReadingTermFuzzy.entryName -> new JapaneseAnalyzer,
        WordField.Definition.entryName       -> new EnglishAnalyzer
      ).asJava
    )

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
}

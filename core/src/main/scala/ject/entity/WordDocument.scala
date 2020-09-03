package ject.entity

import ject.lucene.DocumentDecoder
import ject.lucene.schema.WordField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document

import scala.jdk.CollectionConverters._

final case class WordDocument(
  id: String,
  kanjiTerms: Seq[String] = Seq.empty,
  readingTerms: Seq[String] = Seq.empty,
  definitions: Seq[String] = Seq.empty,
  tags: Seq[String] = Seq.empty,
  partsOfSpeech: Seq[String] = Seq.empty
)

object WordDocument {
  implicit val documentDecoder: DocumentDecoder[WordDocument] = new DocumentDecoder[WordDocument] {
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

    def decode(document: Document): WordDocument =
      WordDocument(
        document.get(WordField.Id.entryName),
        document.getValues(WordField.KanjiTerm.entryName).toIndexedSeq,
        document.getValues(WordField.ReadingTerm.entryName).toIndexedSeq,
        document.getValues(WordField.Definition.entryName).toIndexedSeq,
        document.getValues(WordField.Tags.entryName).toIndexedSeq,
        document.getValues(WordField.PartOfSpeech.entryName).toIndexedSeq
      )
  }
}

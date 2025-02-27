package ject.ko.docs

import ject.ko.lucene.field.WordField
import ject.lucene.{DocDecoder, DocEncoder}
import ject.lucene.field.LuceneField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import zio.*

final case class WordDoc(
    id: String,
    hangulTerms: Seq[String],
    hanjaTerms: Seq[String],
    pronunciation: Seq[String],
    definitionsEnglish: Seq[String],
    definitionsJapanese: Seq[String],
    definitionsKorean: Seq[String],
    partsOfSpeech: Seq[String]
) {

  def terms: Seq[String] = hangulTerms ++ hanjaTerms

  def definitions: Seq[String] = definitionsKorean ++ definitionsJapanese ++ definitionsEnglish

  def render: String = {
    val terms = (hangulTerms ++ hanjaTerms).mkString(" ")
    s"$terms\n${definitions.mkString("\n")}"
  }
}

object WordDoc {

  implicit val docDecoder: DocDecoder[WordDoc] = new DocDecoder[WordDoc] {
    val analyzer: Analyzer = LuceneField.perFieldAnalyzer(WordField.values)

    def decode(document: Document): WordDoc =
      WordDoc(
        id = document.get(WordField.Id.entryName),
        hangulTerms = document.getValues(WordField.HangulTerm.entryName).toIndexedSeq,
        hanjaTerms = document.getValues(WordField.HanjaTerm.entryName).toIndexedSeq,
        pronunciation = document.getValues(WordField.Pronunciation.entryName).toIndexedSeq,
        definitionsEnglish = document.getValues(WordField.DefinitionEnglish.entryName).toIndexedSeq,
        definitionsJapanese = document.getValues(WordField.DefinitionJapanese.entryName).toIndexedSeq,
        definitionsKorean = document.getValues(WordField.DefinitionKorean.entryName).toIndexedSeq,
        partsOfSpeech = document.getValues(WordField.PartOfSpeech.entryName).toIndexedSeq
      )
  }

  val docEncoder: DocEncoder[WordDoc] = (a: WordDoc) =>
    ZIO.attempt {
      val doc = new Document()

      doc.add(new StringField(WordField.Id.entryName, a.id, Field.Store.YES))

      a.hangulTerms.foreach { value =>
        doc.add(new StringField(WordField.HangulTerm.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.HangulTermAnalyzed.entryName, value, Field.Store.NO))
      }

      a.hanjaTerms.foreach { value =>
        doc.add(new StringField(WordField.HanjaTerm.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.HanjaTermAnalyzed.entryName, value, Field.Store.NO))
      }

      a.pronunciation.foreach { value =>
        doc.add(new StringField(WordField.Pronunciation.entryName, value, Field.Store.YES))
      }

      a.definitionsEnglish.foreach { value =>
        doc.add(new TextField(WordField.DefinitionEnglish.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.DefinitionEnglishOther.entryName, value, Field.Store.NO))
      }

      a.definitionsJapanese.foreach { value =>
        doc.add(new TextField(WordField.DefinitionJapanese.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.DefinitionJapaneseOther.entryName, value, Field.Store.NO))
      }

      a.definitionsKorean.foreach { value =>
        doc.add(new TextField(WordField.DefinitionKorean.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.DefinitionKoreanOther.entryName, value, Field.Store.NO))
      }

      a.partsOfSpeech.foreach { value =>
        doc.add(new StringField(WordField.PartOfSpeech.entryName, value, Field.Store.YES))
      }

      doc
    }
}

package ject.ko.docs

import ject.docs.Doc
import ject.ko.lucene.field.WordField
import ject.lucene.DocDecoder
import ject.lucene.field.LuceneField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField

final case class WordDoc(
  id: String,
  hangulTerms: Seq[String],
  hanjaTerms: Seq[String],
  pronunciation: Seq[String],
  definitionsEnglish: Seq[String],
  definitionsKorean: Seq[String],
  partsOfSpeech: Seq[String]
) extends Doc {
  def render: String = {
    val terms = (hangulTerms ++ hanjaTerms).mkString(" ")
    s"$terms: ${definitionsEnglish.mkString("\n")}"
  }

  def toLucene: Document = {
    val doc = new Document()

    doc.add(new StringField(WordField.Id.entryName, id, Field.Store.YES))

    hangulTerms.foreach { value =>
      doc.add(new StringField(WordField.HangulTerm.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.HangulTermAnalyzed.entryName, value, Field.Store.NO))
    }

    hanjaTerms.foreach { value =>
      doc.add(new StringField(WordField.HanjaTerm.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.HanjaTermAnalyzed.entryName, value, Field.Store.NO))
    }

    pronunciation.foreach { value =>
      doc.add(new StringField(WordField.Pronunciation.entryName, value, Field.Store.YES))
    }

    definitionsEnglish.foreach { value =>
      doc.add(new TextField(WordField.DefinitionEnglish.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.DefinitionEnglishOther.entryName, value, Field.Store.NO))
    }

    definitionsKorean.foreach { value =>
      doc.add(new TextField(WordField.DefinitionKorean.entryName, value, Field.Store.YES))
      doc.add(new TextField(WordField.DefinitionKoreanOther.entryName, value, Field.Store.NO))
    }

    partsOfSpeech.foreach { value =>
      doc.add(new StringField(WordField.PartOfSpeech.entryName, value, Field.Store.YES))
    }

    doc
  }
}

object WordDoc {
  implicit val documentDecoder: DocDecoder[WordDoc] = new DocDecoder[WordDoc] {
    val analyzer: Analyzer = LuceneField.perFieldAnalyzer(WordField.values)

    def decode(document: Document): WordDoc =
      WordDoc(
        id = document.get(WordField.Id.entryName),
        hangulTerms = document.getValues(WordField.HangulTerm.entryName).toIndexedSeq,
        hanjaTerms = document.getValues(WordField.HanjaTerm.entryName).toIndexedSeq,
        pronunciation = document.getValues(WordField.Pronunciation.entryName).toIndexedSeq,
        definitionsEnglish = document.getValues(WordField.DefinitionEnglish.entryName).toIndexedSeq,
        definitionsKorean = document.getValues(WordField.DefinitionKorean.entryName).toIndexedSeq,
        partsOfSpeech = document.getValues(WordField.PartOfSpeech.entryName).toIndexedSeq
      )
  }
}

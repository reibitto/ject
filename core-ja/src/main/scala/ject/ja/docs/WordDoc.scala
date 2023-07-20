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
import zio.{Task, ZIO}

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
    for {
      doc <- ZIO.attempt {
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

               doc
             }
      _ <- indexInflections(a, doc).when(includeInflections)
    } yield doc
  }

  private def indexInflections(d: WordDoc, document: Document): Task[Unit] = {
    def indexTerms(terms: Seq[String], field: WordField, wordType: WordType): Task[Unit] =
      ZIO.foreachDiscard(terms) { value =>
        ZIO.attempt {
          Inflection.inflectAll(value, wordType).foreach {
            case (_, Right(chunk)) =>
              chunk.foreach { s =>
                document.add(new StringField(field.entryName, s, Field.Store.NO))
              }

            case _ => ()
          }
        }
      }

    val wordTypeOpt =
      if (d.partsOfSpeech.contains("adj-i"))
        Some(WordType.AdjectiveI)
      else if (d.partsOfSpeech.contains("v1"))
        Some(WordType.VerbIchidan)
      else if (d.partsOfSpeech.exists(_.startsWith("v5k-s")))
        Some(WordType.VerbIku)
      else if (d.partsOfSpeech.exists(_.startsWith("v5")))
        Some(WordType.VerbGodan)
      else if (d.partsOfSpeech.contains("vs") || d.partsOfSpeech.contains("vs-s"))
        Some(WordType.VerbSuru)
      else
        None

    ZIO.foreachDiscard(wordTypeOpt) { wordType =>
      for {
        _ <- indexTerms(d.kanjiTerms, WordField.KanjiTermInflected, wordType)
        _ <- indexTerms(d.readingTerms, WordField.ReadingTermInflected, wordType)
      } yield ()
    }
  }
}

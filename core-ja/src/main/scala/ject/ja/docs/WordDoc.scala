package ject.ja.docs

import ject.ja.lucene.field.WordField
import ject.ja.text.{Inflection, WordType}
import ject.lucene.{DocDecoder, DocEncoder}
import ject.lucene.field.LuceneField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.*
import zio.*

final case class WordDoc(
    id: String,
    kanjiTerms: Seq[String],
    readingTerms: Seq[String],
    definitions: Seq[String],
    tags: Seq[String],
    partsOfSpeech: Seq[String],
    priority: Double,
    frequency: Int
) {

  def terms: Seq[String] = kanjiTerms ++ readingTerms

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
        partsOfSpeech = document.getValues(WordField.PartOfSpeech.entryName).toIndexedSeq,
        priority = document.get(WordField.Priority.entryName).toDouble,
        frequency = document.get(WordField.Frequency.entryName).toInt
      )
  }

  // KanjiTerm/ReadingTerm (and their inflected counterparts) are keyword-like exact-match fields: one token
  // per value via KeywordTokenizer, matched only with TermQuery/PrefixQuery whose relevance is entirely the
  // hand-tuned BoostQuery weight per clause (see WordReader). Norms would otherwise apply BM25 field-length
  // penalties based on how many alternate kanji/readings an entry happens to have (e.g. a JMDict entry
  // bundling 4 kanji forms into one doc vs. a competing dictionary's single-form entry for the same word),
  // which has nothing to do with actual relevance and can outweigh the dictionary-priority boost.
  private val exactMatchStoredType: FieldType = {
    val ft = new FieldType(TextField.TYPE_STORED)
    ft.setOmitNorms(true)
    ft.freeze()
    ft
  }

  private val exactMatchNotStoredType: FieldType = {
    val ft = new FieldType(TextField.TYPE_NOT_STORED)
    ft.setOmitNorms(true)
    ft.freeze()
    ft
  }

  def docEncoder(includeInflections: Boolean): DocEncoder[WordDoc] = (a: WordDoc) =>
    for {
      doc <- ZIO.attempt {
               val doc = new Document()

               doc.add(new StringField(WordField.Id.entryName, a.id, Field.Store.YES))

               // TextField (not StringField) so the field's width/kana-normalizing analyzer actually runs —
               // StringField is always indexed as a single unanalyzed term regardless of which analyzer is
               // configured for it. KeywordTokenizer still guarantees exactly one term per value, preserving
               // exact-match semantics.
               a.kanjiTerms.foreach { value =>
                 doc.add(new Field(WordField.KanjiTerm.entryName, value, exactMatchStoredType))
                 doc.add(new TextField(WordField.KanjiTermAnalyzed.entryName, value, Field.Store.NO))
               }

               a.readingTerms.foreach { value =>
                 doc.add(new Field(WordField.ReadingTerm.entryName, value, exactMatchStoredType))
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

               doc.add(new StoredField(WordField.Priority.entryName, a.priority))
               doc.add(new DoubleDocValuesField(WordField.Priority.entryName, a.priority))

               doc.add(new StoredField(WordField.Frequency.entryName, a.frequency))
               doc.add(new NumericDocValuesField(WordField.Frequency.entryName, a.frequency))

               doc
             }
      _ <- indexInflections(a, doc).when(includeInflections)
    } yield doc

  private def indexInflections(d: WordDoc, document: Document): Task[Unit] = {
    // TextField, not StringField, so field's kana-normalizing analyzer runs at index time — see WordField.
    // Previously this generated both the native-script and a forced-hiragana copy of every inflected form by
    // hand; the analyzer now folds katakana to hiragana itself, so only one form needs to be indexed.
    def indexTerms(terms: Seq[String], field: WordField, wordType: WordType): Task[Unit] = {
      val allInflections = terms.flatMap { value =>
        Inflection.inflectAll(value, wordType).flatMap {
          case (_, Right(chunk)) => chunk.toChunk
          case _                 => Chunk.empty
        }
      }.distinct

      ZIO.foreachDiscard(allInflections) { value =>
        ZIO.attempt {
          document.add(new Field(field.entryName, value, exactMatchNotStoredType))
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
      else if (d.partsOfSpeech.contains("vs") || d.partsOfSpeech.contains("vs-s") || d.partsOfSpeech.contains("vs-i"))
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

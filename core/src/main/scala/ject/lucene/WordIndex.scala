package ject.lucene

import java.nio.file.Path
import ject.SearchPattern
import ject.entity.WordDocument
import ject.locale.JapaneseText
import ject.lucene.field.WordField
import org.apache.lucene.document.{ Document, Field, StringField, TextField }
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.{ BooleanClause, BooleanQuery }
import zio.stream.ZStream
import zio.{ Task, TaskManaged, ZManaged }

class WordIndex(directory: Path) extends LuceneIndex[WordDocument](directory) {
  def add(entry: WordDocument, writer: IndexWriter): Task[Unit] =
    Task {
      val doc = new Document()

      doc.add(new StringField(WordField.Id.entryName, entry.id, Field.Store.YES))

      entry.kanjiTerms.foreach { value =>
        doc.add(new StringField(WordField.KanjiTerm.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.KanjiTermFuzzy.entryName, value, Field.Store.NO))
      }

      entry.readingTerms.foreach { value =>
        doc.add(new StringField(WordField.ReadingTerm.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.ReadingTermFuzzy.entryName, value, Field.Store.NO))
      }

      entry.definitions.foreach { value =>
        doc.add(new TextField(WordField.Definition.entryName, value, Field.Store.YES))
        doc.add(new TextField(WordField.DefinitionOther.entryName, value, Field.Store.NO))
      }

      entry.tags.foreach { value =>
        doc.add(new StringField(WordField.Tags.entryName, value, Field.Store.YES))
      }

      entry.partsOfSpeech.foreach { value =>
        doc.add(new StringField(WordField.PartOfSpeech.entryName, value, Field.Store.YES))
      }

      writer.addDocument(doc)
      ()
    }

  def search(pattern: SearchPattern): ZStream[Any, Throwable, WordDocument] = {
    import ject.lucene.field.WordField._

    val searchType =
      if (pattern.text.exists(JapaneseText.isKanji))
        SearchType.Kanji
      else if (pattern.text.exists(JapaneseText.isKana))
        SearchType.Reading
      else
        SearchType.Definition

    (pattern, searchType) match {
      case (SearchPattern.Unspecified(text), SearchType.Kanji) =>
        searchRaw(s"${KanjiTerm.entryName}:$text* OR ${KanjiTermFuzzy.entryName}:$text")

      case (SearchPattern.Unspecified(text), SearchType.Reading) =>
        searchRaw(s"${ReadingTerm.entryName}:$text* OR ${ReadingTermFuzzy.entryName}:$text")

      case (SearchPattern.Unspecified(text), SearchType.Definition) =>
        val query = new BooleanQuery.Builder()

        text.split("\\s+").foreach { term =>
          query.add(buildQuery(term, Definition), BooleanClause.Occur.SHOULD)
          query.add(buildQuery(term, DefinitionOther), BooleanClause.Occur.SHOULD)
        }

        searchQuery(query.build())

      case (SearchPattern.Exact(text), SearchType.Kanji) =>
        searchRaw(s"${KanjiTerm.entryName}:$text")

      case (SearchPattern.Exact(text), SearchType.Reading) =>
        searchRaw(s"${ReadingTerm.entryName}:$text")

      case (SearchPattern.Exact(text), SearchType.Definition) =>
        searchRaw(s"${Definition.entryName}:$text")

      case (SearchPattern.Contains(text), SearchType.Kanji) =>
        searchRaw(s"${KanjiTerm.entryName}:*$text*")

      case (SearchPattern.Contains(text), SearchType.Reading) =>
        searchRaw(s"${ReadingTerm.entryName}:*$text*")

      case (SearchPattern.Contains(text), SearchType.Definition) =>
        searchRaw(s"${Definition.entryName}:*$text*")

      case (SearchPattern.Prefix(text), SearchType.Kanji) =>
        searchRaw(s"${KanjiTerm.entryName}:$text*")

      case (SearchPattern.Prefix(text), SearchType.Reading) =>
        searchRaw(s"${ReadingTerm.entryName}:$text*")

      case (SearchPattern.Prefix(text), SearchType.Definition) =>
        searchRaw(s"${Definition.entryName}:$text*")

      case (SearchPattern.Suffix(text), SearchType.Kanji) =>
        searchRaw(s"${KanjiTerm.entryName}:*$text")

      case (SearchPattern.Suffix(text), SearchType.Reading) =>
        searchRaw(s"${ReadingTerm.entryName}:*$text")

      case (SearchPattern.Suffix(text), SearchType.Definition) =>
        searchRaw(s"${Definition.entryName}:*$text")

      case (SearchPattern.Wildcard(text), SearchType.Kanji) =>
        searchRaw(s"${KanjiTerm.entryName}:$text")

      case (SearchPattern.Wildcard(text), SearchType.Reading) =>
        searchRaw(s"${ReadingTerm.entryName}:$text")

      case (SearchPattern.Wildcard(text), SearchType.Definition) =>
        searchRaw(s"${Definition.entryName}:$text")

      case (SearchPattern.Raw(text), _) => searchRaw(text)

    }
  }

  sealed trait SearchType
  object SearchType {
    case object Kanji      extends SearchType
    case object Reading    extends SearchType
    case object Definition extends SearchType
  }
}

object WordIndex {
  def make(directory: Path): TaskManaged[WordIndex] =
    ZManaged.succeed(new WordIndex(directory))
}

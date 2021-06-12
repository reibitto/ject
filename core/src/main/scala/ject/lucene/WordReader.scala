package ject.lucene

import ject.SearchPattern
import ject.docs.WordDoc
import ject.locale.JapaneseText
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import zio.TaskManaged
import zio.stream.ZStream

import java.nio.file.Path

final case class WordReader(index: LuceneReader[WordDoc]) {
  def search(pattern: SearchPattern): ZStream[Any, Throwable, ScoredDoc[WordDoc]] = {
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
        // We do both exact search and wildcard search because doing them separately like this will score the exact
        // match higher.
        index.searchRaw(
          s"${KanjiTerm.entryName}:$text OR ${KanjiTerm.entryName}:$text* OR ${KanjiTermFuzzy.entryName}:$text"
        )

      case (SearchPattern.Unspecified(text), SearchType.Reading) =>
        index.searchRaw(
          s"${ReadingTerm.entryName}:$text OR ${ReadingTerm.entryName}:$text* OR ${ReadingTermFuzzy.entryName}:$text"
        )

      case (SearchPattern.Unspecified(text), SearchType.Definition) =>
        val query = new BooleanQuery.Builder()

        text.split("\\s+").foreach { term =>
          query.add(index.buildQuery(term, Definition), BooleanClause.Occur.SHOULD)
          query.add(index.buildQuery(term, DefinitionOther), BooleanClause.Occur.SHOULD)
        }

        index.searchQuery(query.build())

      case (SearchPattern.Exact(text), SearchType.Kanji) =>
        index.searchRaw(s"${KanjiTerm.entryName}:$text")

      case (SearchPattern.Exact(text), SearchType.Reading) =>
        index.searchRaw(s"${ReadingTerm.entryName}:$text")

      case (SearchPattern.Exact(text), SearchType.Definition) =>
        index.searchRaw(s"${Definition.entryName}:$text")

      case (SearchPattern.Contains(text), SearchType.Kanji) =>
        index.searchRaw(s"${KanjiTerm.entryName}:*$text*")

      case (SearchPattern.Contains(text), SearchType.Reading) =>
        index.searchRaw(s"${ReadingTerm.entryName}:*$text*")

      case (SearchPattern.Contains(text), SearchType.Definition) =>
        index.searchRaw(s"${Definition.entryName}:*$text*")

      case (SearchPattern.Prefix(text), SearchType.Kanji) =>
        index.searchRaw(s"${KanjiTerm.entryName}:$text*")

      case (SearchPattern.Prefix(text), SearchType.Reading) =>
        index.searchRaw(s"${ReadingTerm.entryName}:$text*")

      case (SearchPattern.Prefix(text), SearchType.Definition) =>
        index.searchRaw(s"${Definition.entryName}:$text*")

      case (SearchPattern.Suffix(text), SearchType.Kanji) =>
        index.searchRaw(s"${KanjiTerm.entryName}:*$text")

      case (SearchPattern.Suffix(text), SearchType.Reading) =>
        index.searchRaw(s"${ReadingTerm.entryName}:*$text")

      case (SearchPattern.Suffix(text), SearchType.Definition) =>
        index.searchRaw(s"${Definition.entryName}:*$text")

      case (SearchPattern.Wildcard(text), SearchType.Kanji) =>
        index.searchRaw(s"${KanjiTerm.entryName}:$text")

      case (SearchPattern.Wildcard(text), SearchType.Reading) =>
        index.searchRaw(s"${ReadingTerm.entryName}:$text")

      case (SearchPattern.Wildcard(text), SearchType.Definition) =>
        index.searchRaw(s"${Definition.entryName}:$text")

      case (SearchPattern.Raw(text), _) => index.searchRaw(text)

    }
  }

  sealed trait SearchType
  object SearchType {
    case object Kanji      extends SearchType
    case object Reading    extends SearchType
    case object Definition extends SearchType
  }
}

object WordReader {
  def make(directory: Path): TaskManaged[WordReader] =
    for {
      reader <- LuceneReader.make[WordDoc](directory)
    } yield WordReader(reader)
}

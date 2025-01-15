package ject.ja.lucene

import ject.ja.docs.WordDoc
import ject.ja.lucene.field.WordField
import ject.ja.lucene.WordReader.SearchType
import ject.ja.JapaneseText
import ject.lucene.field.LuceneField
import ject.lucene.AnalyzerExtensions.*
import ject.lucene.BooleanQueryBuilderExtensions.*
import ject.lucene.LuceneReader
import ject.lucene.ScoredDoc
import ject.SearchPattern
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queries.function.FunctionScoreQuery
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.util.QueryBuilder
import zio.*
import zio.stream.ZStream

import java.nio.file.Path

final case class WordReader(directory: MMapDirectory, reader: DirectoryReader, searcher: IndexSearcher)
    extends LuceneReader[WordDoc] {
  private val builder = new QueryBuilder(WordDoc.docDecoder.analyzer)

  private val queryParser: QueryParser = new QueryParser(LuceneField.none.entryName, WordDoc.docDecoder.analyzer) {
    setAllowLeadingWildcard(true)
  }

  def search(pattern: SearchPattern): ZStream[Any, Throwable, ScoredDoc[WordDoc]] = {
    val searchType =
      if (pattern.text.exists(JapaneseText.isKanji))
        SearchType.Kanji
      else if (pattern.text.exists(JapaneseText.isKana))
        SearchType.Reading
      else
        SearchType.Definition

    def searchTypeToField(searchType: SearchType): WordField = searchType match {
      case SearchType.Kanji      => WordField.KanjiTerm
      case SearchType.Reading    => WordField.ReadingTerm
      case SearchType.Definition => WordField.Definition
    }

    val booleanQueryTask = ZIO.attempt {
      val booleanQuery = new BooleanQuery.Builder()

      (pattern, searchType) match {
        case (SearchPattern.Default(text), SearchType.Kanji) =>
          val prefixScoreBoost = text.length match {
            case 1 => 10
            case 2 => 50
            case 3 => 100
            case _ => 1000
          }

          Set(text, JapaneseText.toHiragana(text), JapaneseText.toKatakana(text)).map { t =>
            val exactMatchBoost = if (text == t) 1.0f else 0.95f

            booleanQuery.addPrefixQuery(
              WordField.KanjiTerm,
              t,
              BooleanClause.Occur.SHOULD,
              prefixScoreBoost * exactMatchBoost
            )

            booleanQuery.addPhraseQuery(builder)(
              WordField.KanjiTermAnalyzed,
              t,
              BooleanClause.Occur.SHOULD,
              5 * exactMatchBoost
            )
            booleanQuery.addBooleanQuery(builder)(
              WordField.KanjiTerm,
              t,
              BooleanClause.Occur.SHOULD,
              5 * exactMatchBoost
            )
            booleanQuery.addBooleanQuery(builder)(
              WordField.KanjiTermAnalyzed,
              t,
              BooleanClause.Occur.SHOULD,
              1 * exactMatchBoost
            )
            booleanQuery.addTermQuery(WordField.KanjiTermInflected, t, BooleanClause.Occur.SHOULD, 50 * exactMatchBoost)
            booleanQuery.addTermQuery(WordField.KanjiTerm, t, BooleanClause.Occur.SHOULD, 10_000 * exactMatchBoost)
          }.head

        case (SearchPattern.Default(text), SearchType.Reading) =>
          val prefixScoreBoost = text.length match {
            case 1 => 10
            case 2 => 50
            case 3 => 100
            case _ => 1000
          }

          Set(text, JapaneseText.toHiragana(text), JapaneseText.toKatakana(text)).map { t =>
            val exactMatchBoost = if (text == t) 1.0f else 0.95f

            booleanQuery.addPrefixQuery(
              WordField.ReadingTerm,
              t,
              BooleanClause.Occur.SHOULD,
              prefixScoreBoost * exactMatchBoost
            )

            booleanQuery.addPhraseQuery(builder)(
              WordField.ReadingTermAnalyzed,
              t,
              BooleanClause.Occur.SHOULD,
              5 * exactMatchBoost
            )
            booleanQuery.addBooleanQuery(builder)(
              WordField.ReadingTerm,
              t,
              BooleanClause.Occur.SHOULD,
              5 * exactMatchBoost
            )
            booleanQuery.addBooleanQuery(builder)(
              WordField.ReadingTermAnalyzed,
              t,
              BooleanClause.Occur.SHOULD,
              1 * exactMatchBoost
            )
            booleanQuery.addTermQuery(
              WordField.ReadingTermInflected,
              t,
              BooleanClause.Occur.SHOULD,
              50 * exactMatchBoost
            )
            booleanQuery.addTermQuery(WordField.ReadingTerm, t, BooleanClause.Occur.SHOULD, 10_000 * exactMatchBoost)
          }.head

        case (SearchPattern.Exact(text), SearchType.Definition) =>
          booleanQuery.addPhraseQuery(builder)(WordField.Definition, text, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Exact(text), searchType) =>
          booleanQuery.addTermQuery(searchTypeToField(searchType), text, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Default(text), SearchType.Definition) =>
          booleanQuery.addPhraseQuery(builder)(WordField.Definition, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addBooleanQuery(builder)(WordField.Definition, text, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Prefix(text), SearchType.Definition) =>
          val tokens = WordField.Definition.analyzer.tokensFor(text)

          tokens.init.foreach { token =>
            booleanQuery.addTermQuery(WordField.Definition, token, BooleanClause.Occur.SHOULD)
          }

          tokens.lastOption.foreach { token =>
            booleanQuery.addPrefixQuery(WordField.Definition, token, BooleanClause.Occur.SHOULD)
          }

          booleanQuery

        case (pattern @ SearchPattern.Prefix(_), SearchType.Kanji | SearchType.Reading) =>
          // Need to search both Kanji and Reading because the wildcard could be either of them
          booleanQuery.addPrefixQuery(WordField.KanjiTerm, pattern.text, BooleanClause.Occur.SHOULD)
          booleanQuery.addPrefixQuery(WordField.ReadingTerm, pattern.text, BooleanClause.Occur.SHOULD)

        case (pattern @ SearchPattern.Prefix(_), searchType) =>
          booleanQuery.addPrefixQuery(searchTypeToField(searchType), pattern.text, BooleanClause.Occur.SHOULD)

        case (pattern @ SearchPattern.Wildcard(_), SearchType.Kanji | SearchType.Reading) =>
          // Need to search both Kanji and Reading because the wildcard could be either of them
          booleanQuery.addWildcardQuery(WordField.KanjiTerm, pattern.patternText, BooleanClause.Occur.SHOULD)
          booleanQuery.addWildcardQuery(WordField.ReadingTerm, pattern.patternText, BooleanClause.Occur.SHOULD)

        case (pattern @ SearchPattern.Wildcard(_), searchType) =>
          booleanQuery.addWildcardQuery(searchTypeToField(searchType), pattern.patternText, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Raw(text), _) =>
          booleanQuery.add(queryParser.parse(text), BooleanClause.Occur.SHOULD)
      }
    }

    ZStream.unwrap(
      booleanQueryTask.map { b =>
        val query = FunctionScoreQuery.boostByValue(
          b.build(),
          DoubleValuesSource.fromDoubleField(WordField.Priority.entryName)
        )

        searchSorted(
          query,
          new Sort(
            SortField.FIELD_SCORE,
            new SortedNumericSortField(WordField.Frequency.entryName, SortField.Type.INT),
            new SortedNumericSortField(WordField.Priority.entryName, SortField.Type.INT, true)
          )
        )
      }
    )
  }
}

object WordReader {
  sealed trait SearchType

  object SearchType {
    case object Kanji extends SearchType

    case object Reading extends SearchType

    case object Definition extends SearchType
  }

  def make(directory: Path): ZIO[Scope, Throwable, WordReader] =
    LuceneReader.makeReader(directory)(WordReader.apply)
}

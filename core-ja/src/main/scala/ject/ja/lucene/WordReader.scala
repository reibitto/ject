package ject.ja.lucene

import ject.ja.docs.WordDoc
import ject.ja.lucene.field.WordField
import ject.ja.lucene.WordReader.SearchType
import ject.ja.JapaneseText
import ject.lucene.{LuceneReader, ScoredDoc}
import ject.lucene.field.LuceneField
import ject.lucene.AnalyzerExtensions.*
import ject.lucene.BooleanQueryBuilderExtensions.*
import ject.SearchPattern
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import org.apache.lucene.util.QueryBuilder
import zio.{Scope, ZIO}
import zio.stream.ZStream

import java.nio.file.Path

final case class WordReader(index: LuceneReader[WordDoc]) {
  private val builder = new QueryBuilder(WordDoc.docDecoder.analyzer)

  private val queryParser = new QueryParser(LuceneField.none.entryName, WordDoc.docDecoder.analyzer)
  queryParser.setAllowLeadingWildcard(true)

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
          if (text.length > 1)
            booleanQuery.addPrefixQuery(WordField.KanjiTerm, text, BooleanClause.Occur.SHOULD, 1_000)

          booleanQuery.addPhraseQuery(builder)(WordField.KanjiTermAnalyzed, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.KanjiTerm, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.KanjiTermAnalyzed, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addTermQuery(WordField.KanjiTermInflected, text, BooleanClause.Occur.SHOULD, 50)
          booleanQuery.addTermQuery(WordField.KanjiTerm, text, BooleanClause.Occur.SHOULD, 10_000)

        case (SearchPattern.Default(text), SearchType.Reading) =>
          if (text.length > 1)
            booleanQuery.addPrefixQuery(WordField.ReadingTerm, text, BooleanClause.Occur.SHOULD, 1_000)

          booleanQuery.addPhraseQuery(builder)(WordField.ReadingTermAnalyzed, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.ReadingTerm, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.ReadingTermAnalyzed, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addTermQuery(WordField.ReadingTermInflected, text, BooleanClause.Occur.SHOULD, 50)
          booleanQuery.addTermQuery(WordField.ReadingTerm, text, BooleanClause.Occur.SHOULD, 10_000)

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

        case (pattern @ SearchPattern.Prefix(_), searchType) =>
          booleanQuery.addPrefixQuery(searchTypeToField(searchType), pattern.text, BooleanClause.Occur.SHOULD)

        case (pattern @ SearchPattern.Wildcard(_), searchType) =>
          booleanQuery.addWildcardQuery(searchTypeToField(searchType), pattern.patternText, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Raw(text), _) =>
          booleanQuery.add(queryParser.parse(text), BooleanClause.Occur.SHOULD)
      }
    }

    ZStream.unwrap(
      booleanQueryTask.map(b => index.search(b.build()))
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
    for {
      reader <- LuceneReader.make[WordDoc](directory)
    } yield WordReader(reader)
}

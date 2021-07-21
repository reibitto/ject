package ject.ja.lucene

import ject.SearchPattern
import ject.ja.JapaneseText
import ject.ja.docs.WordDoc
import ject.lucene.AnalyzerExtensions._
import ject.lucene.BooleanQueryBuilderExtensions._
import ject.ja.lucene.WordReader.SearchType
import ject.lucene.field.LuceneField
import ject.ja.lucene.field.WordField
import ject.lucene.LuceneReader
import ject.lucene.ScoredDoc
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.util.QueryBuilder
import zio.Task
import zio.TaskManaged
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

    val booleanQueryTask = Task {
      val booleanQuery = new BooleanQuery.Builder()

      (pattern, searchType) match {
        case (SearchPattern.Default(text), SearchType.Kanji) =>
          booleanQuery.addPhraseQuery(builder)(WordField.KanjiTermAnalyzed, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.KanjiTerm, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.KanjiTermAnalyzed, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addTermQuery(WordField.KanjiTermInflected, text, BooleanClause.Occur.SHOULD, 50)
          booleanQuery.addTermQuery(WordField.KanjiTerm, text, BooleanClause.Occur.SHOULD, 100)

        case (SearchPattern.Default(text), SearchType.Reading) =>
          booleanQuery.addPhraseQuery(builder)(WordField.ReadingTermAnalyzed, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.ReadingTerm, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.ReadingTermAnalyzed, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addTermQuery(WordField.ReadingTermInflected, text, BooleanClause.Occur.SHOULD, 50)
          booleanQuery.addTermQuery(WordField.ReadingTerm, text, BooleanClause.Occur.SHOULD, 100)

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
    case object Kanji      extends SearchType
    case object Reading    extends SearchType
    case object Definition extends SearchType
  }

  def make(directory: Path): TaskManaged[WordReader] =
    for {
      reader <- LuceneReader.make[WordDoc](directory)
    } yield WordReader(reader)
}

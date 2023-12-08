package ject.ko.lucene

import ject.SearchPattern
import ject.ko.KoreanText
import ject.ko.docs.WordDoc
import ject.ko.lucene.WordReader.SearchType
import ject.ko.lucene.field.WordField
import ject.lucene.AnalyzerExtensions.*
import ject.lucene.BooleanQueryBuilderExtensions.*
import ject.lucene.LuceneReader
import ject.lucene.ScoredDoc
import ject.lucene.field.LuceneField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.util.QueryBuilder
import zio.Scope
import zio.ZIO
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
      if (pattern.text.exists(KoreanText.isHanja))
        SearchType.Hanja
      else if (pattern.text.exists(KoreanText.isKorean))
        SearchType.Hangul
      else
        SearchType.Definition

    def searchTypeToField(searchType: SearchType): WordField = searchType match {
      case SearchType.Hangul     => WordField.HangulTerm
      case SearchType.Hanja      => WordField.HanjaTerm
      case SearchType.Definition => WordField.DefinitionEnglish
    }

    val booleanQueryTask = ZIO.attempt {
      val booleanQuery = new BooleanQuery.Builder()

      (pattern, searchType) match {
        case (SearchPattern.Default(text), SearchType.Hangul) =>
          booleanQuery.addPhraseQuery(builder)(WordField.HangulTermAnalyzed, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.HangulTerm, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.HangulTermAnalyzed, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addTermQuery(WordField.HangulTerm, text, BooleanClause.Occur.SHOULD, 100)

        case (SearchPattern.Default(text), SearchType.Hanja) =>
          booleanQuery.addPhraseQuery(builder)(WordField.HanjaTermAnalyzed, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.HanjaTerm, text, BooleanClause.Occur.SHOULD, 5)
          booleanQuery.addBooleanQuery(builder)(WordField.HanjaTermAnalyzed, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addTermQuery(WordField.HanjaTerm, text, BooleanClause.Occur.SHOULD, 100)

        case (SearchPattern.Exact(text), SearchType.Definition) =>
          booleanQuery.addPhraseQuery(builder)(WordField.DefinitionEnglish, text, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Exact(text), searchType) =>
          booleanQuery.addTermQuery(searchTypeToField(searchType), text, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Default(text), SearchType.Definition) =>
          booleanQuery.addPhraseQuery(builder)(WordField.DefinitionEnglish, text, BooleanClause.Occur.SHOULD)
          booleanQuery.addBooleanQuery(builder)(WordField.DefinitionEnglish, text, BooleanClause.Occur.SHOULD)

        case (SearchPattern.Prefix(text), SearchType.Definition) =>
          val tokens = WordField.DefinitionEnglish.analyzer.tokensFor(text)

          tokens.init.foreach { token =>
            booleanQuery.addTermQuery(WordField.DefinitionEnglish, token, BooleanClause.Occur.SHOULD)
          }

          tokens.lastOption.foreach { token =>
            booleanQuery.addPrefixQuery(WordField.DefinitionEnglish, token, BooleanClause.Occur.SHOULD)
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
      booleanQueryTask.map(b => search(b.build()))
    )
  }
}

object WordReader {
  sealed trait SearchType

  object SearchType {
    case object Hangul extends SearchType

    case object Hanja extends SearchType

    case object Definition extends SearchType
  }

  def make(directory: Path): ZIO[Scope, Throwable, WordReader] =
    LuceneReader.makeReader(directory)(WordReader.apply)

}

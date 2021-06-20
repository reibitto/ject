package ject.lucene

import ject.SearchPattern
import ject.docs.WordDoc
import ject.locale.JapaneseText
import ject.lucene.AnalyzerExtensions._
import ject.lucene.WordReader.SearchType
import ject.lucene.field.LuceneField
import ject.lucene.field.WordField
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.util.QueryBuilder
import zio.Task
import zio.TaskManaged
import zio.stream.ZStream

import java.nio.file.Path

final case class WordReader(index: LuceneReader[WordDoc]) {
  private val builder = new QueryBuilder(WordDoc.documentDecoder.analyzer)

  private val queryParser = new QueryParser(LuceneField.none.entryName, WordDoc.documentDecoder.analyzer)
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
          booleanQuery.add(
            new BoostQuery(builder.createPhraseQuery(WordField.KanjiTermAnalyzed.entryName, text), 5),
            BooleanClause.Occur.SHOULD
          )
          booleanQuery.add(
            new BoostQuery(builder.createBooleanQuery(WordField.KanjiTerm.entryName, text), 5),
            BooleanClause.Occur.SHOULD
          )
          booleanQuery.add(
            new BoostQuery(builder.createBooleanQuery(WordField.KanjiTermAnalyzed.entryName, text), 1),
            BooleanClause.Occur.SHOULD
          )
          booleanQuery.add(
            new BoostQuery(new TermQuery(WordField.KanjiTerm.term(text)), 100),
            BooleanClause.Occur.SHOULD
          )

        case (SearchPattern.Default(text), SearchType.Reading) =>
          booleanQuery.add(
            new BoostQuery(builder.createPhraseQuery(WordField.ReadingTermAnalyzed.entryName, text), 5),
            BooleanClause.Occur.SHOULD
          )
          booleanQuery.add(
            new BoostQuery(builder.createBooleanQuery(WordField.ReadingTerm.entryName, text), 5),
            BooleanClause.Occur.SHOULD
          )
          booleanQuery.add(
            new BoostQuery(builder.createBooleanQuery(WordField.ReadingTermAnalyzed.entryName, text), 1),
            BooleanClause.Occur.SHOULD
          )
          booleanQuery.add(
            new BoostQuery(new TermQuery(WordField.ReadingTerm.term(text)), 100),
            BooleanClause.Occur.SHOULD
          )

        case (SearchPattern.Exact(text), SearchType.Definition) =>
          booleanQuery.add(
            builder.createPhraseQuery(WordField.Definition.entryName, text),
            BooleanClause.Occur.SHOULD
          )

        case (SearchPattern.Exact(text), searchType) =>
          booleanQuery.add(
            new TermQuery(searchTypeToField(searchType).term(text)),
            BooleanClause.Occur.SHOULD
          )

        case (SearchPattern.Default(text), SearchType.Definition) =>
          booleanQuery.add(
            new BoostQuery(builder.createPhraseQuery(WordField.Definition.entryName, text), 5),
            BooleanClause.Occur.SHOULD
          )
          booleanQuery.add(
            new BoostQuery(builder.createBooleanQuery(WordField.Definition.entryName, text), 5),
            BooleanClause.Occur.SHOULD
          )

        case (SearchPattern.Prefix(text), SearchType.Definition) =>
          val tokens = WordField.Definition.analyzer.tokensFor(text)

          tokens.init.foreach { token =>
            booleanQuery.add(
              new TermQuery(WordField.Definition.term(token)),
              BooleanClause.Occur.SHOULD
            )
          }

          tokens.lastOption.foreach { token =>
            booleanQuery.add(
              new PrefixQuery(WordField.DefinitionOther.term(token)),
              BooleanClause.Occur.SHOULD
            )
          }

          booleanQuery

        case (pattern @ SearchPattern.Prefix(_), searchType) =>
          booleanQuery.add(
            new PrefixQuery(searchTypeToField(searchType).term(pattern.text)),
            BooleanClause.Occur.SHOULD
          )

        case (SearchPattern.Wildcard(text), SearchType.Definition) =>
          booleanQuery.add(
            new WildcardQuery(WordField.DefinitionOther.term(text)),
            BooleanClause.Occur.SHOULD
          )

        case (pattern @ SearchPattern.Wildcard(_), searchType) =>
          booleanQuery.add(
            new WildcardQuery(searchTypeToField(searchType).term(pattern.patternText)),
            BooleanClause.Occur.SHOULD
          )

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

package ject.ja.lucene

import ject.ja.docs.KanjiDoc
import ject.ja.lucene.field.KanjiField
import ject.ja.RadicalQuery
import ject.lucene.LuceneReader
import ject.lucene.ScoredDoc
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.*
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.util.QueryBuilder
import zio.*
import zio.stream.ZStream

import java.nio.file.Path

final case class KanjiReader(directory: MMapDirectory, reader: DirectoryReader, searcher: IndexSearcher)
    extends LuceneReader[KanjiDoc] {
  val builder = new QueryBuilder(KanjiDoc.docDecoder.analyzer)

  def getByKanji(kanji: String): Task[Option[KanjiDoc]] = {
    val query = new BooleanQuery.Builder()

    query.add(new TermQuery(KanjiField.Kanji.term(kanji)), BooleanClause.Occur.MUST)

    headOption(query.build())
  }

  def searchByParts(parts: String): ZStream[Any, Throwable, ScoredDoc[KanjiDoc]] =
    ZStream.unwrap {
      val kanjiQuery = new BooleanQuery.Builder()

      RadicalQuery.normalize(parts).foreach { part =>
        kanjiQuery.add(
          new TermQuery(KanjiField.Kanji.term(part.toString)),
          BooleanClause.Occur.SHOULD
        )
      }

      for {
        combinedParts <- search(kanjiQuery.build)
                           .map(d => Set(d.doc.parts*) + d.doc.kanji)
                           .runFold(Set.empty[String])(_ ++ _)
        partsQuery = new BooleanQuery.Builder()
        _ = combinedParts.foreach { part =>
              partsQuery.add(
                new TermQuery(KanjiField.Parts.term(part)),
                BooleanClause.Occur.SHOULD
              )
            }
        sort = new Sort(
                 SortField.FIELD_SCORE,
                 new SortedNumericSortField(KanjiField.StrokeCount.entryName, SortField.Type.INT),
                 new SortedNumericSortField(KanjiField.Grade.entryName, SortField.Type.INT),
                 new SortedNumericSortField(KanjiField.Frequency.entryName, SortField.Type.INT)
               )
      } yield searchSorted(partsQuery.build, sort)
    }
}

object KanjiReader {

  def make(directory: Path): ZIO[Scope, Throwable, KanjiReader] =
    LuceneReader.makeReader(directory)(KanjiReader.apply)
}

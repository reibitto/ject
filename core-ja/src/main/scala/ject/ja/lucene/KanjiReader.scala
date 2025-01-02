package ject.ja.lucene

import ject.ja.{KanjiLookalikes, RadicalQuery}
import ject.ja.docs.KanjiDoc
import ject.ja.lucene.field.KanjiField
import ject.lucene.LuceneReader
import ject.lucene.ScoredDoc
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.*
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.util.QueryBuilder
import zio.*
import zio.stream.ZStream

import java.nio.file.Path

final case class KanjiReader(
    directory: MMapDirectory,
    reader: DirectoryReader,
    searcher: IndexSearcher,
    kanjiLookalikeMap: Map[String, Seq[String]]
) extends LuceneReader[KanjiDoc] {
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
        // TODO:: new BoostQuery?
        kanjiQuery.add(
          new TermQuery(KanjiField.Kanji.term(part.toString)),
          BooleanClause.Occur.SHOULD
        )
      }

      val lookalikes = parts.flatMap { kanji =>
        kanjiLookalikeMap.get(kanji.toString)
      }.flatten

      for {
        combinedParts <- search(kanjiQuery.build)
                           .map(d => Set(d.doc.parts*) + d.doc.kanji)
                           .runFold(Set.empty[String])(_ ++ _)
        queryBuilder = new BooleanQuery.Builder()
        _ = combinedParts.foreach { part =>
              queryBuilder.add(
                new TermQuery(KanjiField.Parts.term(part)),
                BooleanClause.Occur.SHOULD
              )

              lookalikes.foreach { lookalike =>
                queryBuilder.add(
                  new TermQuery(KanjiField.Kanji.term(lookalike)),
                  BooleanClause.Occur.SHOULD
                )
              }
            }
        sort = new Sort(
                 SortField.FIELD_SCORE,
                 new SortedNumericSortField(KanjiField.StrokeCount.entryName, SortField.Type.INT),
                 new SortedNumericSortField(KanjiField.Grade.entryName, SortField.Type.INT),
                 new SortedNumericSortField(KanjiField.Frequency.entryName, SortField.Type.INT)
               )
      } yield searchSorted(queryBuilder.build, sort)
    }
}

object KanjiReader {

  def make(directory: Path): ZIO[Scope, Throwable, KanjiReader] =
    for {
      kanjiLookalikes <- KanjiLookalikes.load.runCollect
      kanjiLookalikeMap = kanjiLookalikes.map(v => (v.kanji, v.lookalikes)).toMap
      result <- LuceneReader.makeReader(directory) { (mapDirectory, reader, searcher) =>
                  KanjiReader(mapDirectory, reader, searcher, kanjiLookalikeMap)
                }
    } yield result

}

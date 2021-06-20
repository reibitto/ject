package ject.lucene

import ject.docs.KanjiDoc
import ject.lucene.field.KanjiField
import org.apache.lucene.search._
import org.apache.lucene.util.QueryBuilder
import zio.Task
import zio.TaskManaged
import zio.stream.ZStream

import java.nio.file.Path

final case class KanjiReader(index: LuceneReader[KanjiDoc]) {
  val builder = new QueryBuilder(KanjiDoc.documentDecoder.analyzer)

  def getByKanji(kanji: String): Task[Option[KanjiDoc]] = {
    val query = new BooleanQuery.Builder()

    query.add(new TermQuery(KanjiField.Kanji.term(kanji)), BooleanClause.Occur.MUST)

    index.headOption(query.build())
  }

  def searchByParts(parts: String): ZStream[Any, Throwable, ScoredDoc[KanjiDoc]] =
    ZStream.unwrap {
      val kanjiQuery = new BooleanQuery.Builder()

      parts.foreach { part =>
        kanjiQuery.add(
          new TermQuery(KanjiField.Kanji.term(part.toString)),
          BooleanClause.Occur.SHOULD
        )
      }

      for {
        combinedParts <- index
                           .search(kanjiQuery.build)
                           .map(d => Set(d.doc.parts: _*) + d.doc.kanji)
                           .fold(Set.empty[String])(_ ++ _)
        partsQuery     = new BooleanQuery.Builder()
        _              = combinedParts.foreach { part =>
                           partsQuery.add(
                             new TermQuery(KanjiField.Parts.term(part)),
                             BooleanClause.Occur.SHOULD
                           )
                         }
        sort           = new Sort(
                           SortField.FIELD_SCORE,
                           new SortedNumericSortField(KanjiField.Grade.entryName, SortField.Type.LONG),
                           new SortedNumericSortField(KanjiField.Frequency.entryName, SortField.Type.LONG)
                         )
      } yield index.searchSorted(partsQuery.build, sort)
    }
}

object KanjiReader {
  def make(directory: Path): TaskManaged[KanjiReader] =
    for {
      reader <- LuceneReader.make[KanjiDoc](directory)
    } yield KanjiReader(reader)
}

package ject.ja.lucene

import ject.ja.{KanjiLookalikes, RadicalQuery}
import ject.ja.docs.KanjiDoc
import ject.ja.lucene.field.KanjiField
import ject.lucene.LuceneReader
import ject.lucene.ScoredDoc
import ject.utils.StringExtensions.StringExtension
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.util.QueryBuilder
import zio.*
import zio.stream.ZStream

import java.nio.file.Path

final case class KanjiReader(
    directory: Directory,
    reader: DirectoryReader,
    searcher: IndexSearcher,
    kanjiLookalikeMap: Map[String, Seq[String]]
) extends LuceneReader[KanjiDoc] {
  import KanjiReader.*

  val builder = new QueryBuilder(KanjiDoc.docDecoder.analyzer)

  def getByKanji(kanji: String): Task[Option[KanjiDoc]] = {
    val query = new BooleanQuery.Builder()

    query.add(new TermQuery(KanjiField.Kanji.term(kanji)), BooleanClause.Occur.MUST)

    headOption(query.build())
  }

  /** Finds kanji by the parts/radicals they're made of, e.g. searching "山王" or
    * "山王而" should find 瑞. Each input character is matched three ways,
    * highest-confidence first:
    *
    *   1. Directly, against the target's own (transitively expanded, see
    *      `KanjiDecomposition.transitiveComponents`) component set — covers
    *      both a direct part (田 in 果) and one nested arbitrarily deep (刀 in 昭,
    *      via 昭 -> 召 -> 刀).
    *   2. Via the input character's own components, if it's itself a real kanji
    *      — so typing "利" (which decomposes into 禾+刂) still finds 和, whose
    *      actual part is 禾, even though 利 itself is never anyone's component.
    *   3. Via known lookalikes of the input character — so typing the
    *      easier-to-type-but-technically-wrong 冫 still finds 泪, whose real part
    *      is the visually similar 氵.
    *
    * Matches from multiple input characters (or multiple mechanisms above)
    * accumulate in the score, so kanji matching more parts — and more
    * specific/less common ones, since Lucene's own scoring already favors rarer
    * terms — naturally rank higher.
    */
  def searchByParts(parts: String): ZStream[Any, Throwable, ScoredDoc[KanjiDoc]] =
    ZStream.unwrap {
      val inputChars = RadicalQuery.normalize(parts).codePointIterator.toSeq.distinct

      for {
        inputCharComponents <- ZIO.foreach(inputChars)(getByKanji).map(_.flatten.flatMap(_.components).toSet)

        weightedComponents = {
          val direct = inputChars.map(_ -> directComponentBoost)
          val subComponents = inputCharComponents.toSeq.map(_ -> subComponentBoost)
          val lookalikes = inputChars.flatMap(kanjiLookalikeMap.getOrElse(_, Seq.empty)).map(_ -> lookalikeBoost)

          (direct ++ subComponents ++ lookalikes).groupMapReduce(_._1)(_._2)(_ max _)
        }

        queryBuilder = new BooleanQuery.Builder()
        _ = weightedComponents.foreach { case (component, boost) =>
              queryBuilder.add(
                new BoostQuery(new TermQuery(KanjiField.Components.term(component)), boost),
                BooleanClause.Occur.SHOULD
              )
            }

        sort = new Sort(
                 SortField.FIELD_SCORE,
                 new SortedNumericSortField(KanjiField.StrokeCount.entryName, SortField.Type.INT),
                 new SortedNumericSortField(KanjiField.Grade.entryName, SortField.Type.INT),
                 new SortedNumericSortField(KanjiField.Frequency.entryName, SortField.Type.INT)
               )
      } yield
        if (weightedComponents.isEmpty) ZStream.empty
        else searchSorted(queryBuilder.build, sort)
    }
}

object KanjiReader {

  private val directComponentBoost: Float = 3.0f
  private val subComponentBoost: Float = 1.5f
  private val lookalikeBoost: Float = 1.0f

  private val defaultKanjiLookalikeMap: Task[Map[String, Seq[String]]] =
    KanjiLookalikes.load.runCollect.map(_.map(v => (v.kanji, v.lookalikes)).toMap)

  def make(directory: Path): ZIO[Scope, Throwable, KanjiReader] =
    make(directory, defaultKanjiLookalikeMap)

  /** @param kanjiLookalikeMap
    *   Overridable purely for tests that want deterministic lookalike data
    *   instead of depending on the bundled `kanji-lookalikes.txt` resource.
    */
  def make(directory: Path, kanjiLookalikeMap: Task[Map[String, Seq[String]]]): RIO[Scope, KanjiReader] =
    kanjiLookalikeMap.flatMap { lookalikeMap =>
      LuceneReader.makeReader(directory)(KanjiReader(_, _, _, lookalikeMap))
    }

  def make(directory: Directory): RIO[Scope, KanjiReader] =
    make(directory, defaultKanjiLookalikeMap)

  def make(
      directory: Directory,
      kanjiLookalikeMap: Task[Map[String, Seq[String]]]
  ): RIO[Scope, KanjiReader] =
    kanjiLookalikeMap.flatMap { lookalikeMap =>
      LuceneReader.makeReader(directory)(KanjiReader(_, _, _, lookalikeMap))
    }

}

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
    * Ranking is done in `rankByParts` rather than Lucene's own relevance score
    * (see that method's doc for why).
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

        candidates <-
          if (weightedComponents.isEmpty) ZIO.succeed(Seq.empty) else findCandidates(weightedComponents.keySet)
      } yield ZStream.fromIterable(rankByParts(candidates, weightedComponents))
    }

  /** Finds every doc whose `Components` field contains at least one of the
    * given components. Lucene's own relevance score isn't used for anything
    * here (see `rankByParts`) — this is retrieval only, so plain, unboosted
    * `SHOULD` clauses are enough.
    */
  private def findCandidates(components: Set[String]): Task[Seq[KanjiDoc]] = {
    val query = new BooleanQuery.Builder()
    components.foreach(component =>
      query.add(new TermQuery(KanjiField.Components.term(component)), BooleanClause.Occur.SHOULD)
    )
    val builtQuery = query.build()

    ZIO.attemptBlocking(searcher.count(builtQuery)).flatMap { count =>
      if (count == 0) ZIO.succeed(Seq.empty) else take(builtQuery, count).map(_.map(_.doc))
    }
  }
}

object KanjiReader {

  private val directComponentBoost: Float = 3.0f
  private val subComponentBoost: Float = 1.5f
  private val lookalikeBoost: Float = 1.0f

  /** Ranks candidates by how well their own `components` set overlaps with the
    * query, rather than relying on Lucene's own term-frequency/IDF-based
    * relevance score.
    *
    * Lucene's default similarity turns out to be a poor fit for this field.
    * `Components` is indexed as a `StringField`, which omits norms — so how
    * many components a kanji has makes no difference to its score. And because
    * a handful of common radicals (十, 一, 口, ...) each appear in a huge fraction
    * of the ~20,000-entry decomposition graph, their IDF collapses to nearly
    * zero, so matching them barely moves the score at all. Combined, this means
    * a kanji that only coincidentally shares one common radical with the query
    * can score identically to — or higher than — a kanji that matches every
    * queried component: Lucene has no notion of "the input had two parts and
    * this candidate only has one of them" or "this candidate also has five
    * other components nobody asked about."
    *
    * `score` is the sum of the matched components' boosts, scaled down by two
    * independent fractions:
    *
    *   1. `queryCoverage`: how many of the *query's* (distinct) components this
    *      candidate matched. A candidate matching every typed part outscores
    *      one matching only some of them, even if the partial match is
    *      otherwise a "purer" one — someone who typed two parts is looking for
    *      a kanji with both.
    *   2. `candidatePurity`: how many of the *candidate's own* components were
    *      actually matched. A kanji made up almost entirely of queried parts
    *      outranks one that buries a single matched part among several
    *      unrelated ones.
    */
  private def rankByParts(
      candidates: Seq[KanjiDoc],
      weightedComponents: Map[String, Float]
  ): Seq[ScoredDoc[KanjiDoc]] =
    candidates.map { doc =>
      val matched = doc.components.filter(weightedComponents.contains)
      val matchedBoost = matched.map(weightedComponents).sum
      val queryCoverage = matched.size.toDouble / weightedComponents.size
      val candidatePurity = matched.size.toDouble / doc.components.size
      val score = matchedBoost * queryCoverage * candidatePurity

      ScoredDoc(doc, score)
    }.sortBy { scored =>
      (
        -scored.score,
        scored.doc.strokeCount.minOption.getOrElse(Int.MaxValue),
        scored.doc.grade.getOrElse(Int.MaxValue),
        scored.doc.frequency.getOrElse(Int.MaxValue)
      )
    }

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

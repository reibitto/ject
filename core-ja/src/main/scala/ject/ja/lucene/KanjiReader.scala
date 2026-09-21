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

  /** Finds kanji by the parts/radicals they're made of, e.g. "山王" or "山王而"
    * should find 瑞. Each input character is matched three ways,
    * highest-confidence first:
    *
    *   1. Directly, against the target's own transitively expanded component
    *      set (see `KanjiDecomposition.transitiveComponents`), covering both a
    *      direct part (田 in 果) and one nested arbitrarily deep (刀 in 昭).
    *   2. Via the input character's own components, if it's itself a real
    *      kanji, so typing 秒 (= 禾+少) still finds 和, whose actual part is 禾.
    *   3. Via known lookalikes, so the easier-to-type-but-wrong 冫 still finds
    *      泪, whose real part is the visually similar 氵.
    *
    * Ranking is done in `rankByParts` rather than by Lucene's relevance score.
    */
  def searchByParts(parts: String): ZStream[Any, Throwable, ScoredDoc[KanjiDoc]] =
    ZStream.unwrap {
      val inputChars = RadicalQuery.normalize(parts).codePointIterator.toSeq.distinct

      for {
        inputCharComponents <-
          ZIO
            .foreach(inputChars)(c => getByKanji(c).map(doc => c -> doc.toSeq.flatMap(_.components).toSet))
            .map(_.toMap)

        lookalikesOf = inputChars.map(c => c -> kanjiLookalikeMap.getOrElse(c, Seq.empty).toSet).toMap

        allQueryComponents = inputChars.toSet ++ inputCharComponents.values.flatten ++ lookalikesOf.values.flatten

        candidates <-
          if (allQueryComponents.isEmpty) ZIO.succeed(Seq.empty) else findCandidates(allQueryComponents)
      } yield ZStream.fromIterable(rankByParts(candidates, inputChars, inputCharComponents, lookalikesOf))
    }

  /** Finds every doc whose `Components` field contains at least one of the
    * given components. Retrieval only, since scoring happens in `rankByParts`,
    * so plain unboosted `SHOULD` clauses are enough.
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
    * query, rather than by Lucene's term-frequency/IDF relevance score.
    *
    * Lucene's default similarity is a poor fit here. `Components` is a
    * `StringField`, which omits norms, so a kanji's component count doesn't
    * affect its score. And common radicals (十, 一, 口, ...) appear in so much of
    * the ~20,000-entry decomposition graph that their IDF is near zero. A kanji
    * coincidentally sharing one common radical can therefore outscore one
    * matching every queried component.
    *
    * Matching is per input character rather than per flattened component term,
    * and each input character contributes at most once, taking its best tier
    * (direct > subcomponent > lookalike, see `searchByParts`). Otherwise an
    * input character that decomposes into several components (秒 -> 禾+少)
    * inflates the query into one term per subcomponent, and a candidate
    * containing 秒 literally matches all of them at once, burying the candidate
    * that shares only 禾 (和), which is what the mechanism was meant to surface.
    *
    * `score` is the sum of the matched input characters' boosts, scaled by two
    * independent fractions:
    *
    *   1. `queryCoverage`: how many input characters this candidate matched, in
    *      any tier. Someone who typed two parts wants a kanji with both, even
    *      if a partial match is otherwise purer.
    *   2. `candidatePurity`: how much of the candidate's own component set
    *      those matches account for, so a kanji made almost entirely of queried
    *      parts outranks one burying a single match among unrelated parts.
    */
  private def rankByParts(
      candidates: Seq[KanjiDoc],
      inputChars: Seq[String],
      inputCharComponents: Map[String, Set[String]],
      lookalikesOf: Map[String, Set[String]]
  ): Seq[ScoredDoc[KanjiDoc]] =
    candidates.flatMap { doc =>
      val docComponents = doc.components.toSet

      // Per matched input character: the boost of its best tier, plus the candidate components that the
      // match accounts for (used for candidatePurity).
      val matches = inputChars.flatMap { c =>
        val ownComponents = inputCharComponents.getOrElse(c, Set.empty)

        if (docComponents.contains(c))
          Some(directComponentBoost -> (Set(c) ++ ownComponents.intersect(docComponents)))
        else {
          val subMatch = ownComponents.intersect(docComponents)
          if (subMatch.nonEmpty) Some(subComponentBoost -> subMatch)
          else {
            val lookalikeMatch = lookalikesOf.getOrElse(c, Set.empty).intersect(docComponents)
            Option.when(lookalikeMatch.nonEmpty)(lookalikeBoost -> lookalikeMatch)
          }
        }
      }

      Option.when(matches.nonEmpty) {
        val boostSum = matches.map(_._1).sum
        val explained = matches.flatMap(_._2).toSet
        val queryCoverage = matches.size.toDouble / inputChars.size
        val candidatePurity = explained.size.toDouble / doc.components.size

        ScoredDoc(doc, boostSum * queryCoverage * candidatePurity)
      }
    }
      // Below relevance, tiebreak on how common a kanji is (frequency, then grade) before stroke count.
      // Obscure variant characters have no frequency or grade and often tie with the everyday kanji someone
      // is looking for, and a rare variant can easily have fewer strokes than the common kanji it ties with.
      .sortBy { scored =>
        (
          -scored.score,
          scored.doc.frequency.getOrElse(Int.MaxValue),
          scored.doc.grade.getOrElse(Int.MaxValue),
          scored.doc.strokeCount.minOption.getOrElse(Int.MaxValue)
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

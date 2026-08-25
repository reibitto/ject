package ject.ja.lucene

import ject.ja.docs.KanjiDoc
import ject.ja.entity.KanjiDecomposition
import ject.lucene.LuceneDirectory
import ject.utils.StringExtensions.StringExtension
import zio.*
import zio.test.*

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*

/** End-to-end tests for `KanjiReader.searchByParts` against a real, in-memory
  * Lucene index built from the actual `data/kanji-decomposition.tsv` file, the
  * same file `KanjiDecompositionIO.load` reads in production (see
  * `KanjidicMain`), rather than a small handwritten sample. A handful of
  * entries is nowhere near enough to judge search quality: with only 20-30
  * candidates in the index, almost anything that matches at all ends up looking
  * like a good result. Loading the real ~20,000-entry decomposition graph gives
  * every kanji its real, full set of competing candidates, so a kanji that
  * merely happens to share one part with the query has plenty of (correctly)
  * more-relevant competitors to be outranked by.
  */
object KanjiReaderIntegrationSpec extends ZIOSpecDefault {

  // strokeCount is carried alongside each decomposition purely so the test index can populate KanjiDoc's real
  // stroke count (a sort tiebreaker below relevance) instead of a placeholder that would tie every candidate.
  private def loadDecompositions(file: String): Task[Map[String, (Int, KanjiDecomposition)]] =
    ZIO.attempt {
      Files
        .readAllLines(Paths.get(file))
        .asScala
        .filter(_.trim.nonEmpty)
        .map { line =>
          val tokens = line.split("\t", -1)
          val kanji = tokens(0)
          val strokeCount = tokens(1).toInt
          val firstParts = tokens(3).codePointIterator.filterNot(_ == "*")
          val secondParts = tokens(6).codePointIterator.filterNot(_ == "*")
          val radical = tokens(10).codePointIterator.filterNot(_ == "*")

          kanji -> (
            strokeCount,
            KanjiDecomposition(
              kanji = kanji,
              components = firstParts.toSet ++ secondParts.toSet ++ radical.toSet
            )
          )
        }
        .toMap
    }

  private def kanjiDoc(kanji: String, strokeCount: Int, decompositions: Map[String, KanjiDecomposition]): KanjiDoc =
    KanjiDoc(
      kanji = kanji,
      meaning = Seq.empty,
      onYomi = Seq.empty,
      kunYomi = Seq.empty,
      nanori = Seq.empty,
      koreanReadings = Seq.empty,
      radicalId = 0,
      parts = Seq.empty,
      components = KanjiDecomposition.transitiveComponents(kanji, decompositions).toSeq,
      strokeCount = Seq(strokeCount),
      frequency = None,
      jlpt = None,
      grade = None
    )

  // Only 冫 -> 氵 is needed for these tests. Injected explicitly rather than relying on the bundled
  // kanji-lookalikes.txt resource, so this suite doesn't depend on that file's exact contents.
  private val lookalikeMap: Task[Map[String, Seq[String]]] =
    ZIO.succeed(Map("冫" -> Seq("氵")))

  /** Built once and shared across every test in this suite (via
    * `provideLayerShared` below) rather than per-test, since indexing is an
    * expensive operation.
    */
  private val kanjiReaderLayer: ZLayer[Any, Throwable, KanjiReader] =
    ZLayer.scoped {
      for {
        rows <- loadDecompositions("data/kanji-decomposition.tsv")
        decompositions = rows.map { case (kanji, (_, decomposition)) => kanji -> decomposition }
        entries = rows.map { case (kanji, (strokeCount, _)) =>
                    kanjiDoc(kanji, strokeCount, decompositions)
                  }.toSeq
        directory <- LuceneDirectory.inMemory
        _         <- ZIO.scoped(KanjiWriter.make(directory).flatMap(_.addBulk(entries*)))
        reader    <- KanjiReader.make(directory, lookalikeMap)
      } yield reader
    }

  private def kanjiFound(query: String): ZIO[KanjiReader, Throwable, List[String]] =
    ZIO.serviceWithZIO[KanjiReader](_.searchByParts(query).runCollect.map(_.toList.map(_.doc.kanji)))

  /** A kanji doesn't need to be the #1 result to be a good result, but it
    * should reliably show up near the top, not be buried somewhere a real user
    * would never scroll to.
    */
  private val defaultTopN = 5

  private def ranksNear(query: String, kanji: String, topN: Int = defaultTopN) =
    kanjiFound(query).map(results => assertTrue(results.take(topN).contains(kanji)))

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("KanjiReader.searchByParts")(
      test("finds 瑞 via its direct components 山王")(ranksNear("山王", "瑞")),
      test("finds 瑞 via all three of its direct components 山王而")(ranksNear("山王而", "瑞")),
      test("finds 和 via 秒口, where 秒 is not itself a component of 和 but shares 和's actual component 禾")(
        ranksNear("秒口", "和")
      ),
      test("finds 昨 via its direct components 日作, where 作 shares 昨's actual component 乍")(
        ranksNear("日作", "昨")
      ),
      test("finds 昭 via 日刀, where 刀 is a component two levels deep (昭 -> 召 -> 刀)")(
        ranksNear("日刀", "昭")
      ),
      test("finds 果 via its direct components 田木")(ranksNear("田木", "果")),
      test("finds 泪 via ;目, where ; expands to the lookalike-but-wrong radical 冫 instead of 泪's actual 氵")(
        ranksNear(";目", "泪")
      ),
      test("ranks a kanji matching more input parts above one matching fewer") {
        // 昭 matches both 日 and 刀 (the latter two levels deep); 昨 only matches 日. 昭 should rank first.
        kanjiFound("日刀").map { results =>
          assertTrue(results.contains("昭"), results.contains("昨"), results.indexOf("昭") < results.indexOf("昨"))
        }
      },
      test("does not find anything for parts matching no known kanji") {
        // U+E000 is in the Unicode Private Use Area, guaranteed not to appear anywhere in the real dictionary.
        kanjiFound("").map(results => assertTrue(results.isEmpty))
      },
      test("finds 加 via its direct components 力口")(ranksNear("力口", "加")),
      test("finds 間 via its direct components 日門")(ranksNear("日門", "間")),
      test("finds 計 via its direct components 十言")(ranksNear("十言", "計")),
      test("finds 件 via 人牛, matching the 人 form even though 件's decomposition also separately lists 亻")(
        ranksNear("人牛", "件")
      ),
      test("finds 陣 via 車阝, where 阝 is the compact radical form listed directly in 陣's decomposition")(
        ranksNear("車阝", "陣")
      ),
      test("finds 姫 via its direct components 女臣")(ranksNear("女臣", "姫")),
      test("finds 丁 via its direct components 一亅, i.e. bare strokes rather than named radicals")(
        ranksNear("一亅", "丁")
      ),
      test("finds 詩 via its direct components 言寺")(ranksNear("言寺", "詩")),
      test("finds 詩 via 言土, where 土 is a component two levels deep (詩 -> 寺 -> 土)") {
        // A slightly wider window than usual: several real kanji genuinely match 言 and 土 more precisely
        // than 詩 does (e.g. 詿 = 言 + 圭, and 圭 itself is just two 土 stacked, with no other components at
        // all), so 詩 — which also carries 寺's other part 寸 — fairly ranks a bit further down than 5th.
        ranksNear("言土", "詩", topN = 10)
      },
      test("finds 怒 via 又心, where 又 is a component one level deep via 怒's direct component 奴 (奴 -> 又, 女)")(
        ranksNear("又心", "怒")
      ),
      test("finds both 昭 and 詔, since both transitively decompose through 召 (-> 刀, 口)") {
        kanjiFound("刀口").map(results => assertTrue(results.contains("昭"), results.contains("詔")))
      },
      test("ranks 詔 above 昭 when 言 is included, since only 詔 has 言 as a direct component") {
        kanjiFound("言召刀口").map { results =>
          assertTrue(results.contains("詔"), results.contains("昭"), results.indexOf("詔") < results.indexOf("昭"))
        }
      }
    ).provideLayerShared(kanjiReaderLayer)
}

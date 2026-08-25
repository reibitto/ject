package ject.ja.lucene

import ject.ja.docs.KanjiDoc
import ject.ja.entity.KanjiDecomposition
import ject.lucene.LuceneDirectory
import zio.*
import zio.test.*

/** End-to-end tests for `KanjiReader.searchByParts` against a real, in-memory
  * Lucene index built from a small hand-written sample of kanji and their real
  * decompositions, rather than assuming the scoring/matching logic is correct.
  * Each `KanjiDoc.components` value below is derived via
  * `KanjiDecomposition.transitiveComponents` from the `decompositions` map, the
  * same function `KanjidicIO.load` uses when building a real index — so this
  * also exercises the recursive-expansion logic itself, not just the search
  * half.
  */
object KanjiReaderIntegrationSpec extends ZIOSpecDefault {

  // A tiny decomposition graph covering the examples this feature exists for. 召 decomposes further than 昭's
  // direct entry mentions, so 昭's expanded components pick up 刀/口 transitively (昭 -> 召 -> 刀, 口).
  //
  // The entries below (加 through 怒) are real decompositions pulled from data/kanji-decomposition.tsv, the same
  // source KanjidicIO.load reads in production, added to exercise the algorithm against genuine dictionary data
  // rather than only this suite's originally hand-picked examples. 寺 and 奴 have no KanjiDoc of their own (like
  // 召) — they only exist here as intermediate nodes so 詩 and 怒 expand transitively through them.
  private val decompositions: Map[String, KanjiDecomposition] = Map(
    "瑞" -> KanjiDecomposition("瑞", Set("王", "山", "而")),
    "和" -> KanjiDecomposition("和", Set("禾", "口")),
    "利" -> KanjiDecomposition("利", Set("禾", "刂")),
    "昨" -> KanjiDecomposition("昨", Set("日", "乍")),
    "作" -> KanjiDecomposition("作", Set("亻", "乍")),
    "昭" -> KanjiDecomposition("昭", Set("日", "召")),
    "召" -> KanjiDecomposition("召", Set("刀", "口")),
    "果" -> KanjiDecomposition("果", Set("田", "木")),
    "泪" -> KanjiDecomposition("泪", Set("氵", "目")),
    "加" -> KanjiDecomposition("加", Set("力", "口")),
    "間" -> KanjiDecomposition("間", Set("日", "門")),
    "計" -> KanjiDecomposition("計", Set("十", "言")),
    "企" -> KanjiDecomposition("企", Set("人", "止")),
    "件" -> KanjiDecomposition("件", Set("人", "亻", "牛")),
    "団" -> KanjiDecomposition("団", Set("囗", "寸")),
    "図" -> KanjiDecomposition("図", Set("囗", "斗")),
    "内" -> KanjiDecomposition("内", Set("人", "冂")),
    "詩" -> KanjiDecomposition("詩", Set("寺", "言")),
    "寺" -> KanjiDecomposition("寺", Set("土", "寸")),
    "詔" -> KanjiDecomposition("詔", Set("召", "言")),
    "陣" -> KanjiDecomposition("陣", Set("車", "阜", "阝")),
    "偉" -> KanjiDecomposition("偉", Set("人", "亻", "韋")),
    "姫" -> KanjiDecomposition("姫", Set("女", "臣")),
    "丁" -> KanjiDecomposition("丁", Set("一", "亅")),
    "怒" -> KanjiDecomposition("怒", Set("奴", "心")),
    "奴" -> KanjiDecomposition("奴", Set("又", "女"))
  )

  private def kanjiDoc(kanji: String, strokeCount: Int): KanjiDoc =
    KanjiDoc(
      kanji = kanji,
      meaning = Seq(s"test meaning for $kanji"),
      onYomi = Seq.empty,
      kunYomi = Seq.empty,
      nanori = Seq.empty,
      koreanReadings = Seq.empty,
      radicalId = 1,
      parts = Seq.empty,
      components = KanjiDecomposition.transitiveComponents(kanji, decompositions).toSeq,
      strokeCount = Seq(strokeCount),
      frequency = None,
      jlpt = None,
      grade = None
    )

  // The 6 target kanji from the feature's own examples, plus 利 and 作 — both need their own KanjiDoc entries
  // since search-by-parts looks up an input character's own components when it's itself a real kanji (e.g.
  // typing "利" should also match on its components 禾/刂, even though 利 itself is never anyone's component).
  private val sampleEntries: Seq[KanjiDoc] = Seq(
    kanjiDoc("瑞", 13),
    kanjiDoc("和", 8),
    kanjiDoc("利", 7),
    kanjiDoc("昨", 9),
    kanjiDoc("作", 7),
    kanjiDoc("昭", 9),
    kanjiDoc("果", 8),
    kanjiDoc("泪", 8),
    kanjiDoc("加", 5),
    kanjiDoc("間", 12),
    kanjiDoc("計", 9),
    kanjiDoc("企", 6),
    kanjiDoc("件", 6),
    kanjiDoc("団", 6),
    kanjiDoc("図", 7),
    kanjiDoc("内", 4),
    kanjiDoc("詩", 13),
    kanjiDoc("詔", 12),
    kanjiDoc("陣", 10),
    kanjiDoc("偉", 12),
    kanjiDoc("姫", 10),
    kanjiDoc("丁", 2),
    kanjiDoc("怒", 9)
  )

  // Only 冫 -> 氵 is needed for these tests. Injected explicitly rather than relying on the bundled
  // kanji-lookalikes.txt resource, so this suite doesn't depend on that file's exact contents.
  private val lookalikeMap: Task[Map[String, Seq[String]]] =
    ZIO.succeed(Map("冫" -> Seq("氵")))

  private def withSampleIndex[A](f: KanjiReader => Task[A]): Task[A] =
    ZIO.scoped {
      for {
        directory <- LuceneDirectory.inMemory
        _         <- ZIO.scoped(KanjiWriter.make(directory).flatMap(_.addBulk(sampleEntries*)))
        reader    <- KanjiReader.make(directory, lookalikeMap)
        result    <- f(reader)
      } yield result
    }

  private def kanjiFound(reader: KanjiReader, query: String): Task[List[String]] =
    reader.searchByParts(query).runCollect.map(_.toList.map(_.doc.kanji))

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("KanjiReader.searchByParts")(
      test("finds 瑞 via its direct components 山王") {
        withSampleIndex(kanjiFound(_, "山王")).map(results => assertTrue(results.contains("瑞")))
      },
      test("finds 瑞 via all three of its direct components 山王而") {
        withSampleIndex(kanjiFound(_, "山王而")).map(results => assertTrue(results.contains("瑞")))
      },
      test("finds 和 via 利口, where 利 is not itself a component of 和 but shares 和's actual component 禾") {
        withSampleIndex(kanjiFound(_, "利口")).map(results => assertTrue(results.contains("和")))
      },
      test("finds 昨 via its direct components 日作, where 作 shares 昨's actual component 乍") {
        withSampleIndex(kanjiFound(_, "日作")).map(results => assertTrue(results.contains("昨")))
      },
      test("finds 昭 via 日刀, where 刀 is a component two levels deep (昭 -> 召 -> 刀)") {
        withSampleIndex(kanjiFound(_, "日刀")).map(results => assertTrue(results.contains("昭")))
      },
      test("finds 果 via its direct components 田木") {
        withSampleIndex(kanjiFound(_, "田木")).map(results => assertTrue(results.contains("果")))
      },
      test("finds 泪 via ;目, where ; expands to the lookalike-but-wrong radical 冫 instead of 泪's actual 氵") {
        withSampleIndex(kanjiFound(_, ";目")).map(results => assertTrue(results.contains("泪")))
      },
      test("ranks a kanji matching more input parts above one matching fewer") {
        // 昭 matches both 日 and 刀 (the latter two levels deep); 昨 only matches 日. 昭 should rank first.
        withSampleIndex(kanjiFound(_, "日刀")).map { results =>
          assertTrue(results.contains("昭"), results.contains("昨"), results.indexOf("昭") < results.indexOf("昨"))
        }
      },
      test("does not find anything for parts matching no known kanji") {
        withSampleIndex(kanjiFound(_, "龍")).map(results => assertTrue(results.isEmpty))
      },

      // The tests below use real decompositions pulled from data/kanji-decomposition.tsv rather than this
      // suite's originally hand-picked examples, to exercise the algorithm against a broader, independently
      // sourced sample of genuine dictionary data.
      test("finds 加 via its direct components 力口") {
        withSampleIndex(kanjiFound(_, "力口")).map(results => assertTrue(results.contains("加")))
      },
      test("finds 間 via its direct components 日門") {
        withSampleIndex(kanjiFound(_, "日門")).map(results => assertTrue(results.contains("間")))
      },
      test("finds 計 via its direct components 十言") {
        withSampleIndex(kanjiFound(_, "十言")).map(results => assertTrue(results.contains("計")))
      },
      test("finds 件 via 人牛, matching the 人 form even though 件's decomposition also separately lists 亻") {
        withSampleIndex(kanjiFound(_, "人牛")).map(results => assertTrue(results.contains("件")))
      },
      test("finds 陣 via 車阝, where 阝 is the compact radical form listed directly in 陣's decomposition") {
        withSampleIndex(kanjiFound(_, "車阝")).map(results => assertTrue(results.contains("陣")))
      },
      test("finds 姫 via its direct components 女臣") {
        withSampleIndex(kanjiFound(_, "女臣")).map(results => assertTrue(results.contains("姫")))
      },
      test("finds 丁 via its direct components 一亅, i.e. bare strokes rather than named radicals") {
        withSampleIndex(kanjiFound(_, "一亅")).map(results => assertTrue(results.contains("丁")))
      },
      test("finds 詩 via its direct components 言寺") {
        withSampleIndex(kanjiFound(_, "言寺")).map(results => assertTrue(results.contains("詩")))
      },
      test("finds 詩 via 言土, where 土 is a component two levels deep (詩 -> 寺 -> 土)") {
        withSampleIndex(kanjiFound(_, "言土")).map(results => assertTrue(results.contains("詩")))
      },
      test("finds 怒 via 又心, where 又 is a component one level deep via 怒's direct component 奴 (奴 -> 又, 女)") {
        withSampleIndex(kanjiFound(_, "又心")).map(results => assertTrue(results.contains("怒")))
      },
      test("finds both 昭 and 詔, since both transitively decompose through 召 (-> 刀, 口)") {
        withSampleIndex(kanjiFound(_, "刀口")).map(results => assertTrue(results.contains("昭"), results.contains("詔")))
      },
      test("ranks 詔 above 昭 when 言 is included, since only 詔 has 言 as a direct component") {
        withSampleIndex(kanjiFound(_, "言召刀口")).map { results =>
          assertTrue(results.contains("詔"), results.contains("昭"), results.indexOf("詔") < results.indexOf("昭"))
        }
      }
    )
}

package ject.ja.entity

import zio.*
import zio.test.*

object KanjiDecompositionSpec extends ZIOSpecDefault {

  private val decompositions: Map[String, KanjiDecomposition] = Map(
    "昭" -> KanjiDecomposition("昭", Set("日", "召")),
    "召" -> KanjiDecomposition("召", Set("刀", "口")),
    "果" -> KanjiDecomposition("果", Set("田", "木"))
  )

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("KanjiDecomposition.transitiveComponents")(
      test("returns the direct components for a single-level decomposition") {
        assertTrue(KanjiDecomposition.transitiveComponents("果", decompositions) == Set("田", "木"))
      },
      test("recursively expands nested decompositions, not just the direct level") {
        // 昭 -> 日, 召; 召 -> 刀, 口. Both the direct components (日, 召) and 召's own components (刀, 口)
        // are included — 召 is itself a real, findable kanji, so it stays alongside what it further expands to.
        assertTrue(KanjiDecomposition.transitiveComponents("昭", decompositions) == Set("日", "召", "刀", "口"))
      },
      test("returns an empty set for a kanji with no known decomposition") {
        assertTrue(KanjiDecomposition.transitiveComponents("龍", decompositions) == Set.empty[String])
      },
      test("does not loop forever on cyclic decomposition data") {
        val cyclic = Map(
          "甲" -> KanjiDecomposition("甲", Set("乙")),
          "乙" -> KanjiDecomposition("乙", Set("甲"))
        )

        assertTrue(KanjiDecomposition.transitiveComponents("甲", cyclic, maxDepth = 4) == Set("乙"))
      }
    )
}

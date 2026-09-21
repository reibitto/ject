package ject.ja.entity

final case class KanjiDecomposition(kanji: String, components: Set[String])

object KanjiDecomposition {

  /** All components reachable from `kanji` by repeatedly expanding each
    * component into its own components, not just the direct ones. E.g. 昭
    * decomposes into {日, 召} and 召 into {刀, 口}, so 昭 yields {日, 召, 刀, 口}, making
    * 刀 findable two levels deep.
    *
    * `maxDepth` and the `visited` cycle guard are a safety net against
    * malformed or cyclic data; well-formed data bottoms out at atomic radicals.
    */
  def transitiveComponents(
      kanji: String,
      decompositions: Map[String, KanjiDecomposition],
      maxDepth: Int = 8
  ): Set[String] = {
    def go(current: String, depth: Int, visited: Set[String]): Set[String] =
      if (depth >= maxDepth)
        Set.empty
      else
        decompositions.get(current) match {
          case Some(decomposition) =>
            val direct = decomposition.components -- visited
            direct ++ direct.flatMap(c => go(c, depth + 1, visited ++ direct))

          case None =>
            Set.empty
        }

    go(kanji, 0, Set(kanji))
  }
}

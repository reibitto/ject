package ject.ja.entity

final case class KanjiDecomposition(kanji: String, components: Set[String])

object KanjiDecomposition {

  /** All components reachable from `kanji` by repeatedly expanding each
    * component into its own components, not just the immediate/direct ones.
    * E.g. if 昭 decomposes directly into {日, 召}, and 召 itself further decomposes
    * into {刀, 口}, this returns {日, 召, 刀, 口} for 昭 — surfacing "刀" as a findable
    * part even though it's two levels deep, not a direct component of 昭.
    *
    * `maxDepth` and the `visited` cycle guard exist purely as a safety net
    * against malformed/cyclic decomposition data; well-formed data always
    * bottoms out at atomic radicals with no further decomposition.
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

package ject.tools.other

import ject.ja.entity.KanjiDecomposition
import ject.utils.StringExtensions.StringExtension
import zio.stream.{ZPipeline, ZStream}
import zio.RIO

import java.nio.file.Path

object KanjiDecompositionIO {

  def load(file: Path): RIO[Any, Map[String, KanjiDecomposition]] =
    ZStream
      .fromPath(file)
      .via(ZPipeline.utf8Decode)
      .via(ZPipeline.splitLines)
      .map { line =>
        val tokens = line.split("\t")
        val kanji = tokens(0)
        val firstParts = tokens(3).codePointIterator.filterNot(_ == "*")
        val secondParts = tokens(6).codePointIterator.filterNot(_ == "*")
        val radical = tokens(10).codePointIterator.filterNot(_ == "*")

        kanji -> KanjiDecomposition(
          kanji = kanji,
          components = firstParts.toSet ++ secondParts.toSet ++ radical.toSet
        )
      }
      .runCollect
      .map(_.toMap)
}

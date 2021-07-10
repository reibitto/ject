package ject.ja.io

import ject.ja.entity.KanjiComposition
import ject.ja.entity.KanjiPart
import zio.Task
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.stream.ZTransducer

import java.nio.file.Path

object KanjiDecompositionIO {
  def load(file: Path): ZStream[Blocking, Any, KanjiPart] =
    ZStream
      .fromFile(file)
      .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
      .mapM { line =>
        Task {
          val tokens = line.split('\t')
          val comp   = (tokens(2), tokens(3), tokens(6)) match {
            case ("一" | "*", a, _) => KanjiComposition.Primitive(a)
            case ("吅", a, b)       => KanjiComposition.Horizontal(a.map(_.toString), b.map(_.toString))
            case ("吕", a, b)       => KanjiComposition.Vertical(a.map(_.toString), b.map(_.toString))
            case ("回", a, b)       => KanjiComposition.Inclusion(a.map(_.toString), b.map(_.toString))
            case ("咒", a, b)       => KanjiComposition.VerticalRepetition(a.map(_.toString), b.map(_.toString))
            case ("弼", a, b)       => KanjiComposition.HorizontalSurround(a.map(_.toString), b.map(_.toString))
            case ("品", a, _)       => KanjiComposition.Repetition3(a.map(_.toString))
            case ("叕", a, _)       => KanjiComposition.Repetition4(a.map(_.toString))
            case ("冖", a, b)       => KanjiComposition.VerticalHat(a.map(_.toString), b.map(_.toString))
            case ("+", a, b)       => KanjiComposition.Additive(a.map(_.toString), b.map(_.toString))
            case other             => throw new Exception(s"Unexpected kanji composition: $other")
          }

          KanjiPart(character = tokens(0), strokeCount = tokens(1).toInt, composition = comp, radical = tokens(10))
        }
      }
}

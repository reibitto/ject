package ject.io

import java.nio.file.Path

import ject.entity.Radical
import zio.RIO
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.stream.ZTransducer

object RadicalIO {
  def load(file: Path): RIO[Blocking, Map[String, Radical]] =
    ZStream
      .fromFile(file)
      .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
      .zipWithIndex
      .map { case (line, index) =>
        val tokens  = line.split("\t")
        val radical = tokens(0)

        (
          radical,
          Radical(
            index.toInt + 1,
            radical,
            tokens(1).map(_.toString).toSet,
            tokens(2),
            tokens.lift(4).getOrElse("").map(_.toString).toSet
          )
        )
      }
      .runCollect
      .map(_.toMap)
}

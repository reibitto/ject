package ject.ja.io

import ject.ja.entity.Radical
import zio.RIO
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.stream.ZTransducer

import java.nio.file.Path

object RadicalIO {
  def load(file: Path): RIO[Blocking, Map[String, Radical]] =
    ZStream
      .fromFile(file)
      .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
      .zipWithIndex
      .map { case (line, index) =>
        val tokens  = line.split("\t")
        val radical = tokens(0)

        radical -> Radical(
          radicalId = index.toInt + 1,
          radical = radical,
          variants = tokens(1).map(_.toString).toSet,
          name = tokens(2),
          kanji = tokens.lift(4).getOrElse("").map(_.toString).toSet + radical
        )
      }
      .runCollect
      .map(_.toMap)
}

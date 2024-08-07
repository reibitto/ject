package ject.tools.jmdict

import ject.ja.entity.Radical
import zio.stream.{ZPipeline, ZStream}
import zio.RIO

import java.nio.file.Path

object RadicalIO {

  def load(file: Path): RIO[Any, Map[String, Radical]] =
    ZStream
      .fromPath(file)
      .via(ZPipeline.utf8Decode)
      .via(ZPipeline.splitLines)
      .zipWithIndex
      .map { case (line, index) =>
        val tokens = line.split("\t")
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

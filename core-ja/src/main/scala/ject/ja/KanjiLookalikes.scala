package ject.ja

import zio.stream.{ZPipeline, ZStream}

import java.io.IOException

final case class KanjiLookalike(kanji: String, lookalikes: Seq[String])

object KanjiLookalikes {

  def load: ZStream[Any, IOException, KanjiLookalike] =
    ZStream
      .fromResource("kanji-lookalikes.txt")
      .via(ZPipeline.utf8Decode)
      .via(ZPipeline.splitLines)
      .map { line =>
        val parts = line.split("[\t,]")

        KanjiLookalike(
          kanji = parts.head,
          lookalikes = parts.tail.toSeq
        )
      }

}

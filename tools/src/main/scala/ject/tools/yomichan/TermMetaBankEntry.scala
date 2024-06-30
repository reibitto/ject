package ject.tools.yomichan

import ject.ja.entity.FrequencyEntry
import zio.NonEmptyChunk

sealed trait TermMetaBankEntry

object TermMetaBankEntry {

  final case class Frequency(term: String, reading: String, frequency: Int) extends TermMetaBankEntry {

    def toFrequencyEntry: FrequencyEntry =
      FrequencyEntry(term, reading, frequency)
  }

  final case class Pitch(term: String, reading: String, pitches: NonEmptyChunk[Int]) extends TermMetaBankEntry
}

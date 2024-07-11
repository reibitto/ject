package ject.ja.entity

import zio.Chunk

final case class Frequencies(data: Map[String, Chunk[FrequencyEntry]]) {

  def findMostCommon(term: String): FrequencyEntry = {
    val frequencyEntries = data.getOrElse(term, Chunk(FrequencyEntry.emptyFor(term)))

    frequencyEntries.minByOption(_.frequency).getOrElse(FrequencyEntry.emptyFor(term))
  }

  def find(terms: Seq[String], readings: Seq[String]): FrequencyEntry =
    Chunk
      .fromIterable(terms)
      .map { term =>
        data.getOrElse(term, Chunk.empty).filter { f =>
          readings.contains(f.reading)
        }
      }
      .flatten
      .minByOption(_.frequency)
      .getOrElse(FrequencyEntry.emptyFor(""))
}

object Frequencies {
  def empty: Frequencies = Frequencies(Map.empty)
}

final case class FrequencyEntry(term: String, reading: String, frequency: Int)

object FrequencyEntry {
  def emptyFor(term: String): FrequencyEntry = FrequencyEntry(term, term, Int.MaxValue)
}

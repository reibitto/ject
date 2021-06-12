package ject.lucene

import ject.docs.KanjiDoc
import ject.lucene.field.KanjiField
import zio.TaskManaged
import zio.stream.ZStream

import java.nio.file.Path

final case class KanjiReader(index: LuceneReader[KanjiDoc]) {
  def searchByParts(parts: String): ZStream[Any, Throwable, ScoredDoc[KanjiDoc]] =
    ZStream.unwrap {
      val kanjiQuery = parts.map(part => s"${KanjiField.Kanji.entryName}:$part").mkString(" ")

      for {
        combinedParts <- index
                           .searchRaw(kanjiQuery)
                           .map(d => Set(d.doc.kanji) ++ Set(d.doc.parts: _*))
                           .fold(Set.empty[String])(_ ++ _)
        partsQuery     = combinedParts.map(part => s"${KanjiField.Parts.entryName}:$part").mkString(" ")
      } yield index.searchRaw(partsQuery)
    }
}

object KanjiReader {
  def make(directory: Path): TaskManaged[KanjiReader] =
    for {
      reader <- LuceneReader.make[KanjiDoc](directory)
    } yield KanjiReader(reader)
}

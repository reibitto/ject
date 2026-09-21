package ject.examples

import ject.tools.jmdict.KanjidicIO
import ject.utils.IOExtensions.*
import ject.utils.NumericExtensions.LongExtension
import zio.*
import zio.Console.printLine

import java.nio.file.Paths

/** Regenerates the kanji frequency/grade TSV that `KanjiReaderIntegrationSpec`
  * indexes. Run this after picking up a newer kanjidic release.
  */
object KanjiFrequencyMain extends ZIOAppDefault {

  def run: Task[Unit] = {
    val kanjidicPath = Paths.get("data/dictionary/kanjidic.xml")
    val outputPath = Paths.get("core-ja/src/test/resources/kanji-frequency.tsv")

    for {
      _     <- kanjidicPath.ensureDirectoryExists()
      _     <- KanjidicIO.download(kanjidicPath).unless(kanjidicPath.toFile.exists())
      count <- KanjidicIO.exportFrequenciesAndGrades(kanjidicPath, outputPath)
      _     <- printLine(s"Wrote ${count.groupSeparated} entries to ${outputPath.toFile.getCanonicalPath}")
    } yield ()
  }
}

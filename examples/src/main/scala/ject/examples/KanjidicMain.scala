package ject.examples

import ject.ja.lucene.KanjiWriter
import ject.tools.jmdict.KanjidicIO
import ject.tools.jmdict.RadicalIO
import ject.utils.IOExtensions.*
import zio.*
import zio.Console.printLine

import java.nio.file.Paths

object KanjidicMain extends ZIOAppDefault {

  val dryRun: Boolean = false

  def run: UIO[Unit] =
    (for {
      _        <- printLine(s"Starting to index dictionary: Kanjidic")
      radicals <- RadicalIO.load(Paths.get("data/radicals.dat"))
      targetPath = Paths.get("data/dictionary/kanjidic.xml")
      _ <- targetPath.ensureDirectoryExists()
      _ <- KanjidicIO.download(targetPath).unless(targetPath.toFile.exists())
      luceneDirectory = Paths.get("data/lucene/kanji")
      (timeTaken, totalDocs) <- ZIO.scoped {
                                  for {
                                    index <- KanjiWriter.make(luceneDirectory)
                                    count <- KanjidicIO
                                               .load(targetPath, radicals)
                                               .grouped(100)
                                               .mapZIO { entries =>
                                                 index.addBulk(entries*).unless(dryRun).as(entries)
                                               }
                                               .flattenChunks
                                               .zipWithIndex
                                               .mapZIO { case (_, n) =>
                                                 printLine(s"Imported $n entries...")
                                                   .when(n > 0 && n % 10_000 == 0)
                                                   .as(n)
                                               }
                                               .runLast
                                               .map(_.getOrElse(0))
                                  } yield count
                                }.timed
      _ <- printLine(s"Indexed $totalDocs entries (completed in ${timeTaken.render})")
      _ <- printLine(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")

    } yield ()).catchAllCause { t =>
      ZIO.succeed(t.squash.printStackTrace())
    }

}

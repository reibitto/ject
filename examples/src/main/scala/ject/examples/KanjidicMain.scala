package ject.examples

import ject.ja.io.{ KanjidicIO, RadicalIO }
import ject.ja.lucene.KanjiWriter
import ject.utils.IOExtensions._
import zio.Console.printLine
import zio._

import java.nio.file.Paths

object KanjidicMain extends zio.ZIOAppDefault {
  def run: URIO[Any, ExitCode] =
    (for {
      radicals               <- RadicalIO.load(Paths.get("data/radicals.dat"))
      targetPath              = Paths.get("data/dictionary/kanjidic.xml")
      _                      <- targetPath.ensureDirectoryExists()
      _                      <- KanjidicIO.download(targetPath).unless(targetPath.toFile.exists())
      luceneDirectory         = Paths.get("data/lucene")
      (timeTaken, totalDocs) <- ZIO.scoped {
                                  for {
                                    index <- KanjiWriter.make(luceneDirectory.resolve("kanji"))
                                    _     <- KanjidicIO
                                               .load(targetPath, radicals)
                                               .zipWithIndex
                                               .mapZIO { case (entry, n) =>
                                                 for {
                                                   _ <- printLine(s"Imported $n entries...").when(n > 0 && n % 1000 == 0)
                                                   _ <- index.add(entry)
                                                 } yield n
                                               }
                                               .runLast
                                               .map(_.getOrElse(0))
                                  } yield ()
                                }.timed
      _                      <- printLine(s"Indexed $totalDocs entries (completed in ${timeTaken.render})")
      _                      <- printLine(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")

    } yield ()).exitCode

}

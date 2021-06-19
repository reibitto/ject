package ject.examples

import ject.io.KanjidicIO
import ject.io.RadicalIO
import ject.lucene.KanjiWriter
import ject.utils.IOExtensions._
import zio._
import zio.console._
import zio.duration._

import java.nio.file.Paths

object KanjidicMain extends zio.App {
  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (for {
      radicals               <- RadicalIO.load(Paths.get("data/radicals.dat"))
      targetPath              = Paths.get("data/dictionary/kanjidic.xml")
      _                      <- targetPath.ensureDirectoryExists()
      _                      <- KanjidicIO.download(targetPath)
      luceneDirectory         = Paths.get("data/lucene")
      (timeTaken, totalDocs) <- KanjiWriter
                                  .make(luceneDirectory.resolve("kanji"))
                                  .use { index =>
                                    KanjidicIO
                                      .load(targetPath, radicals)
                                      .zipWithIndex
                                      .mapM { case (entry, n) =>
                                        for {
                                          _ <- putStrLn(s"Imported $n entries...").when(n > 0 && n % 1000 == 0)
                                          _ <- index.add(entry)
                                        } yield n
                                      }
                                      .runLast
                                      .map(_.getOrElse(0))
                                  }
                                  .timed
      _                      <- putStrLn(s"Indexed $totalDocs entries (completed in ${timeTaken.render})")
      _                      <- putStrLn(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")

    } yield ()).exitCode

}

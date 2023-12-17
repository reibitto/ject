package ject.examples

import ject.ja.lucene.KanjiWriter
import ject.tools.jmdict.KanjidicIO
import ject.tools.jmdict.RadicalIO
import ject.utils.IOExtensions.*
import zio.*
import zio.Console.printLine

import java.nio.file.Paths

object KanjidicMain extends ZIOAppDefault {

  def run: UIO[Unit] =
    (for {
      radicals <- RadicalIO.load(Paths.get("data/radicals.dat"))
      targetPath = Paths.get("data/dictionary/kanjidic.xml")
      _ <- targetPath.ensureDirectoryExists()
      _ <- KanjidicIO.download(targetPath).unless(targetPath.toFile.exists())
      luceneDirectory = Paths.get("data/lucene")
      (timeTaken, totalDocs) <- ZIO.scoped {
                                  for {
                                    index <- KanjiWriter.make(luceneDirectory.resolve("kanji"))
                                    count <- KanjidicIO
                                               .load(targetPath, radicals)
                                               .zipWithIndex
                                               .mapZIO { case (entry, n) =>
                                                 for {
                                                   _ <-
                                                     printLine(s"Imported $n entries...").when(n > 0 && n % 1000 == 0)
                                                   _ <- index.add(entry)
                                                 } yield n
                                               }
                                               .runLast
                                               .map(_.getOrElse(0))
                                  } yield count
                                }.timed
      _ <- printLine(s"Indexed $totalDocs entries (completed in ${timeTaken.render})")
      _ <- printLine(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")

    } yield ()).tapError { t =>
      ZIO.succeed(t.printStackTrace())
    }.exitCode.flatMap(exit)

}

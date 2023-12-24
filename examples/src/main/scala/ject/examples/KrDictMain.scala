package ject.examples

import ject.ko.lucene.WordWriter
import ject.tools.krdict.KrDictIO
import ject.DefinitionLanguage
import zio.*
import zio.Console.printLine

import java.nio.file.Paths

object KrDictMain extends ZIOAppDefault {

  val dryRun: Boolean = false

  def run: UIO[Unit] =
    (for {
      _ <- printLine(s"Starting to index dictionary: KrDict")
      targetPath = Paths.get("data/dictionary/krdict-ja")
      definitionLanguage = DefinitionLanguage.Japanese
      // targetPath = Paths.get("data/dictionary/naver-kr-jp")
      // definitionLanguage = DefinitionLanguage.Japanese
      luceneDirectory <- ZIO.succeed(Paths.get("data/lucene/word-ko"))
      (timeTaken, totalDocs) <- ZIO.scoped {
                                  for {
                                    index <- WordWriter.make(luceneDirectory)
                                    count <- KrDictIO
                                               .load(targetPath, definitionLanguage)
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
                                  } yield count
                                }.timed
      _ <- printLine(s"Indexed $totalDocs entries (completed in ${timeTaken.render})")
      _ <- printLine(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")
    } yield ()).tapErrorCause { t =>
      ZIO.succeed(t.squash.printStackTrace())
    }.exitCode.flatMap(exit)

}

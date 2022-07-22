package ject.examples

import ject.ja.docs.WordDoc
import ject.ja.io.JMDictIO
import ject.ja.lucene.WordWriter
import ject.utils.IOExtensions.*
import zio.*
import zio.Console.printLine

import java.io.File
import java.nio.file.Paths

object JMDictMain extends zio.ZIOAppDefault {

  def run: URIO[Any, ExitCode] = {
    val targetPath = Paths.get("data/dictionary/JMDict_e.xml")

    (for {
      _        <- targetPath.ensureDirectoryExists()
      tempFile <- ZIO.attempt(File.createTempFile("JMDict", ""))
      _        <- JMDictIO.download(tempFile.toPath).unless(targetPath.toFile.exists())
      _        <- JMDictIO.normalize(tempFile.toPath, targetPath).unless(targetPath.toFile.exists())
      luceneDirectory = Paths.get("data/lucene")
      (timeTaken, totalDocs) <- ZIO.scoped {
                                  for {
                                    index <- WordWriter
                                               .make(
                                                 luceneDirectory.resolve("word"),
                                                 WordDoc.docEncoder(includeInflections = true)
                                               )
                                    _ <- JMDictIO
                                           .load(targetPath)
                                           .zipWithIndex
                                           .mapZIO { case (entry, n) =>
                                             for {
                                               _ <- printLine(s"Imported $n entries...").when(n > 0 && n % 10000 == 0)
                                               _ <- index.add(entry)
                                             } yield n
                                           }
                                           .runLast
                                           .map(_.getOrElse(0))
                                  } yield ()
                                }.timed
      _ <- printLine(s"Indexed $totalDocs entries (completed in ${timeTaken.render})")
      _ <- printLine(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")

    } yield ()).exitCode
  }

}

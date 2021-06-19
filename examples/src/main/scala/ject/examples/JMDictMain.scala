package ject.examples

import ject.io.JMDictIO
import ject.lucene.WordWriter
import ject.utils.IOExtensions._
import zio._
import zio.console._
import zio.duration._

import java.io.File
import java.nio.file.Paths

object JMDictMain extends zio.App {
  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val targetPath = Paths.get("data/dictionary/JMDict_e.xml")

    (for {
      _                      <- targetPath.ensureDirectoryExists()
      tempFile               <- Task(File.createTempFile("JMDict", ""))
      _                      <- JMDictIO.download(tempFile.toPath)
      _                      <- JMDictIO.normalize(tempFile.toPath, targetPath)
      luceneDirectory         = Paths.get("data/lucene")
      (timeTaken, totalDocs) <- WordWriter
                                  .make(luceneDirectory.resolve("word"))
                                  .use { index =>
                                    JMDictIO
                                      .load(targetPath)
                                      .zipWithIndex
                                      .mapM { case (entry, n) =>
                                        for {
                                          _ <- putStrLn(s"Imported $n entries...").when(n > 0 && n % 10000 == 0)
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

}

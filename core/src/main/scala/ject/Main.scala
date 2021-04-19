package ject

import ject.io.JMDictIO
import ject.lucene.WordIndex
import ject.utils.IOExtensions._
import zio._
import zio.console._

import java.io.File
import java.nio.file.Paths

object Main extends zio.App {
  // TODO: Eventually add command line parser and options to import other dictionaries
  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val targetPath = Paths.get("data/dictionary/JMDict_e.xml")

    (for {
      _              <- targetPath.ensureDirectoryExists()
      tempFile       <- Task(File.createTempFile("JMDict", ""))
      _              <- JMDictIO.download(tempFile.toPath)
      _              <- JMDictIO.normalize(tempFile.toPath, targetPath)
      luceneDirectory = Paths.get("data/lucene")
      wordIndex       = new WordIndex(luceneDirectory.resolve("word"))
      writer          = wordIndex.createWriter
      _              <- JMDictIO
                          .load(targetPath)
                          .zipWithIndex
                          .foreach { case (entry, n) =>
                            for {
                              _ <- putStrLn(s"Imported $n entries...").when(n > 0 && n % 10000 == 0)
                              _ <- wordIndex.add(entry, writer)
                            } yield ()
                          }
      _              <- Task(writer.commit())
      _              <- Task(writer.close())
      _              <- putStrLn(s"Indexing complete. Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")
    } yield ()).exitCode
  }

}

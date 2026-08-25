package ject.examples

import ject.ja.docs.WordDoc
import ject.ja.entity.Frequencies
import ject.ja.lucene.WordWriter
import ject.tools.jmdict.JMDictIO
import ject.tools.yomichan.TermMetaBankIO
import ject.utils.IOExtensions.*
import ject.utils.NumericExtensions.*
import zio.*
import zio.Console.printLine

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object JMDictMain extends ZIOAppDefault {

  val dryRun: Boolean = false

  def run: Task[Unit] =
    for {
      entries <- TermMetaBankIO.loadFrequencies(Paths.get("data/dictionary/bccwj-luw")).runCollect
      frequencies = Frequencies(entries.groupBy(_.term).map { case (k, v) =>
                      k -> v.map(_.toFrequencyEntry)
                    })
      _ <- printLine(s"Starting to index dictionary: JMDict")
      targetPath = Paths.get("data/dictionary/JMDict_e.xml")
      _ <- targetPath.ensureDirectoryExists()
      _ <- ZIO.unless(targetPath.toFile.exists()) {
             ZIO.acquireReleaseWith(ZIO.attempt(File.createTempFile("JMDict", "")))(tempFile =>
               ZIO.attempt(tempFile.delete()).ignore
             ) { tempFile =>
               JMDictIO.download(tempFile.toPath) *> JMDictIO.normalize(tempFile.toPath, targetPath)
             }
           }
      fileTime <- ZIO.attempt(Files.getLastModifiedTime(targetPath))
      _        <- printLine(s"Using JMDict file from $fileTime")
      luceneDirectory = Paths.get("data/lucene/word-ja")
      (timeTaken, totalDocs) <- ZIO.scoped {
                                  for {
                                    index <- WordWriter
                                               .make(
                                                 luceneDirectory,
                                                 WordDoc.docEncoder(includeInflections = true)
                                               )
                                    count <- JMDictIO
                                               .load(targetPath, frequencies)
                                               .grouped(100)
                                               .mapZIO { entries =>
                                                 index.addBulk(entries*).unless(dryRun).as(entries)
                                               }
                                               .flattenChunks
                                               .zipWithIndex
                                               .mapZIO { case (_, n) =>
                                                 printLine(s"Imported ${n.groupSeparated} entries...")
                                                   .when(n > 0 && n % 10_000 == 0)
                                                   .as(n)
                                               }
                                               .runLast
                                               .map(_.getOrElse(0L))
                                  } yield count
                                }.timed
      _ <- printLine(s"Indexed ${totalDocs.groupSeparated} entries (completed in ${timeTaken.render})")
      _ <- printLine(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")
    } yield ()

}

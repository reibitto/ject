package ject.examples

import ject.ja.docs.WordDoc
import ject.ja.entity.Frequencies
import ject.ja.entity.FrequencyEntry
import ject.ja.lucene.WordWriter
import ject.ja.JapaneseText
import ject.tools.yomichan.TermBankIO
import ject.tools.yomichan.TermMetaBankIO
import zio.*
import zio.Console.printLine

import java.nio.file.Paths

object YomichanMain extends ZIOAppDefault {

  val dryRun: Boolean = false

  final case class DictionaryInfo(name: String, priority: Int, shouldSanitize: Boolean)

  def run: UIO[Unit] = {
    val dictionaries = Seq(
      DictionaryInfo("sanseido", 40, shouldSanitize = true),
      DictionaryInfo("koujien", 30, shouldSanitize = true),
      DictionaryInfo("daijisen", 20, shouldSanitize = true),
      DictionaryInfo("pixiv", 10, shouldSanitize = true)
    )

    (for {
      entries <- TermMetaBankIO.loadFrequencies(Paths.get("data/dictionary/bccwj-luw")).runCollect
      frequencies = Frequencies(entries.groupBy(_.term).map { case (k, v) =>
                      k -> v.map(_.toFrequencyEntry)
                    })
      _ <- ZIO.foreachDiscard(dictionaries) { dictionary =>
             val targetPath = Paths.get(s"data/dictionary/${dictionary.name}")

             for {
               _               <- printLine(s"Starting to index dictionary: ${dictionary.name}")
               luceneDirectory <- ZIO.succeed(Paths.get("data/lucene/word-ja"))
               (timeTaken, totalDocs) <-
                 ZIO.scoped {
                   for {
                     index <- WordWriter.make(luceneDirectory)
                     count <- TermBankIO
                                .load(targetPath)
                                .filter(_.term.nonEmpty)
                                .zipWithIndex
                                .map { case (e, i) =>
                                  // val sanitizedDefinitions = e.definitions.flatMap(_.asText.trim.split("\n{2,}").toVector)

                                  val sanitizedDefinitions =
                                    if (dictionary.shouldSanitize)
                                      e.definitions.headOption match {
                                        case Some(a) =>
                                          val lines = a.asText.linesIterator.toVector

                                          if (lines.length > 1)
                                            (lines
                                              .drop(1)
                                              .mkString("\n")
                                              .trim +: e.definitions.tail.map(_.asText))
                                              .map(_.trim)
                                              .filter(_.nonEmpty)
                                          else
                                            lines

                                        case None =>
                                          Vector.empty
                                      }
                                    else
                                      e.definitions.map(_.asText)

                                  // val sanitizedDefinitions2 = sanitizedDefinitions.flatMap(_.trim.split("\n{2,}").toVector)

                                  val kanjiTerms = Seq(e.term).filter(_.exists(JapaneseText.isKanji))
                                  val readingTerms = e.reading.toSeq
                                  val frequencyEntry = frequencies.find(kanjiTerms, readingTerms)

                                  WordDoc(
                                    id = s"${dictionary.name}/${i + 1}",
                                    kanjiTerms = kanjiTerms,
                                    readingTerms = readingTerms,
                                    definitions = sanitizedDefinitions,
                                    tags = e.termTags ++ e.definitionTags,
                                    partsOfSpeech = e.inflection,
                                    priority = dictionary.priority,
                                    frequency = frequencyEntry.frequency
                                  )
                                }
                                // TODO: Add support for 子 links
                                .filterNot(_.tags.contains("子"))
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
               _ <- printLine(s"Indexed $totalDocs entries (completed in ${timeTaken.render}) (dryRun: ${dryRun})")
               _ <- printLine(s"Index directory is located at ${luceneDirectory.toFile.getCanonicalPath}")
             } yield ()
           }
    } yield ()).catchAllCause { t =>
      ZIO.succeed(t.squash.printStackTrace())
    }
  }

}

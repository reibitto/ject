package ject.examples

import ject.ja.docs.WordDoc
import ject.ja.lucene.WordWriter
import ject.ja.JapaneseText
import ject.tools.yomichan.TermBankIO
import zio.*
import zio.Console.printLine

import java.nio.file.Paths

object YomichanMain extends ZIOAppDefault {

  val dryRun: Boolean = false

  final case class DictionaryInfo(name: String, priority: Long, shouldSanitize: Boolean)

  def run: UIO[Unit] = {
    val dictionaries = Seq(
      DictionaryInfo("sanseido", 4, shouldSanitize = true),
      DictionaryInfo("koujien", 3, shouldSanitize = true),
      DictionaryInfo("daijisen", 2, shouldSanitize = true),
      DictionaryInfo("pixiv", 1, shouldSanitize = true)
    )

    ZIO
      .foreachDiscard(dictionaries) { dictionary =>
        val targetPath = Paths.get(s"data\\dictionary\\${dictionary.name}")

        for {
          _               <- printLine(s"Starting to index dictionary: ${dictionary.name}")
          luceneDirectory <- ZIO.succeed(Paths.get("data/lucene/word-ja"))
          (timeTaken, totalDocs) <- ZIO.scoped {
                                      for {
                                        index <- WordWriter.make(luceneDirectory)
                                        count <- TermBankIO
                                                   .load(targetPath)
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

                                                     WordDoc(
                                                       id = s"${dictionary.name}/${i + 1}",
                                                       kanjiTerms = Seq(e.term).filter(_.exists(JapaneseText.isKanji)),
                                                       readingTerms = e.reading.toSeq,
                                                       definitions = sanitizedDefinitions,
                                                       tags = e.termTags ++ e.definitionTags,
                                                       partsOfSpeech = e.inflection,
                                                       popularity = dictionary.priority
                                                     )
                                                   }
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
      .tapError { t =>
        ZIO.succeed(t.printStackTrace())
      }
      .exitCode
      .flatMap(exit)
  }

}

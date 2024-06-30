package ject.tools.yomichan

import zio.*
import zio.stream.ZStream

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.StreamConverters.*

object TermMetaBankIO {

  def load(dictionaryDirectory: Path): ZStream[Any, Throwable, TermMetaBankEntry] =
    ???

  def loadFrequencies(dictionaryDirectory: Path): ZStream[Any, Throwable, TermMetaBankEntry.Frequency] = {
    val files = Files.list(dictionaryDirectory).toScala(Chunk).filter { f =>
      val filename = f.getFileName.toString
      filename.startsWith("term_meta_bank_") && filename.endsWith(".json")
    }

    ZStream.concatAll(
      files.map { file =>
        ZStream
          .fromIterableZIO(
            for {
              rawJson <- ZIO.blocking(ZIO.succeed(Files.readString(file, StandardCharsets.UTF_8)))
              json    <- ZIO.blocking(ZIO.fromEither(io.circe.parser.parse(rawJson)))
              array   <- ZIO.fromOption(json.asArray).orElseFail(new Exception("Term meta bank must be an array"))
            } yield array
          )
          .mapZIO { entry =>
            ZIO.fromOption(entry.asArray).orElseFail(new Exception("Term meta bank entry must be an array"))
          }
          .map { fields =>
            val term = fields(0).asString.map(_.trim).getOrElse(throw new Exception("Term is empty"))

            val (reading, frequency) = fields(2).asObject match {
              case Some(data) =>
                val reading = data("reading").flatMap(_.asString)

                val frequency = data("frequency")
                  .getOrElse(throw new Exception("frequency does not exist"))
                  .asNumber
                  .getOrElse(throw new Exception("frequency is not a string"))
                  .toDouble
                  .toInt

                (reading.getOrElse(term), frequency)

              case None =>
                fields(2).asNumber match {
                  case Some(frequency) =>
                    (term, frequency.toDouble.toInt)

                  case None =>
                    throw new Exception("data is not a JSON object or number")
                }
            }

            TermMetaBankEntry.Frequency(
              term = fields(0).asString.map(_.trim).getOrElse(throw new Exception("Term is empty")),
              reading = reading,
              frequency = frequency
            )
          }
      }
    )
  }

  def loadPitches(dictionaryDirectory: Path): ZStream[Any, Throwable, TermMetaBankEntry.Pitch] =
    ???
}

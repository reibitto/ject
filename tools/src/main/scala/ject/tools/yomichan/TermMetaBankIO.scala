package ject.tools.yomichan

import zio.*
import zio.stream.ZStream

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object TermMetaBankIO {

  private def listTermMetaBankFiles(dictionaryDirectory: Path): ZStream[Any, Throwable, Path] =
    ZStream.blocking {
      ZStream
        .fromJavaStream(Files.list(dictionaryDirectory))
        .filter { f =>
          val filename = f.getFileName.toString
          filename.startsWith("term_meta_bank_") && filename.endsWith(".json")
        }
    }

  private def loadFields(
      dictionaryDirectory: Path,
      mode: String
  ): ZStream[Any, Throwable, Vector[io.circe.Json]] =
    listTermMetaBankFiles(dictionaryDirectory).flatMap { file =>
      ZStream
        .fromIterableZIO(
          for {
            rawJson <- ZIO.attemptBlocking(Files.readString(file, StandardCharsets.UTF_8))
            json    <- ZIO.attemptBlocking(io.circe.parser.parse(rawJson)).absolve
            array   <- ZIO.fromOption(json.asArray).orElseFail(new Exception("Term meta bank must be an array"))
          } yield array
        )
        .mapZIO { entry =>
          ZIO.fromOption(entry.asArray).orElseFail(new Exception("Term meta bank entry must be an array"))
        }
        .filter(fields => fields(1).asString.contains(mode))
    }

  def load(dictionaryDirectory: Path): ZStream[Any, Throwable, TermMetaBankEntry] =
    loadFrequencies(dictionaryDirectory) ++ loadPitches(dictionaryDirectory)

  def loadFrequencies(dictionaryDirectory: Path): ZStream[Any, Throwable, TermMetaBankEntry.Frequency] =
    loadFields(dictionaryDirectory, "freq").map { fields =>
      val term = fields(0).asString.map(_.trim).getOrElse(throw new Exception("Term is empty"))

      val (reading, frequency) = fields(2).asObject match {
        case Some(data) =>
          val reading = data("reading").flatMap(_.asString)

          val frequency = data("frequency")
            .getOrElse(throw new Exception("frequency does not exist"))
            .asNumber
            .getOrElse(throw new Exception("frequency is not a number"))
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
        term = term,
        reading = reading,
        frequency = frequency
      )
    }

  def loadPitches(dictionaryDirectory: Path): ZStream[Any, Throwable, TermMetaBankEntry.Pitch] =
    loadFields(dictionaryDirectory, "pitch").map { fields =>
      val term = fields(0).asString.map(_.trim).getOrElse(throw new Exception("Term is empty"))

      val data = fields(2).asObject.getOrElse(throw new Exception("Pitch data must be a JSON object"))
      val reading = data("reading").flatMap(_.asString).getOrElse(term)

      val pitches = data("pitches")
        .flatMap(_.asArray)
        .getOrElse(throw new Exception("Pitch data must contain a \"pitches\" array"))
        .flatMap { pitch =>
          pitch.asObject.flatMap(_("position")).flatMap(_.asNumber).flatMap(_.toInt)
        }

      TermMetaBankEntry.Pitch(
        term = term,
        reading = reading,
        pitches = NonEmptyChunk
          .fromIterableOption(pitches)
          .getOrElse(throw new Exception("Pitch data must contain at least one pitch entry"))
      )
    }
}

package ject.tools.yomichan

import zio.*
import zio.stream.ZStream

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.StreamConverters.*

object TermBankIO {

  def load(dictionaryDirectory: Path): ZStream[Any, Throwable, TermBankEntry] = {
    val files = Files.list(dictionaryDirectory).toScala(Chunk).filter { f =>
      val filename = f.getFileName.toString
      filename.startsWith("term_bank_") && filename.endsWith(".json")
    }

    ZStream.concatAll(
      files.map { file =>
        ZStream
          .fromIterableZIO(
            for {
              rawJson <- ZIO.attemptBlocking(Files.readString(file, StandardCharsets.UTF_8))
              json    <- ZIO.blocking(ZIO.fromEither(io.circe.parser.parse(rawJson)))
              array   <- ZIO.fromOption(json.asArray).orElseFail(new Exception("Term bank must be an array"))
            } yield array
          )
          .mapZIO { entry =>
            ZIO.fromOption(entry.asArray).orElseFail(new Exception("Term bank entry must be an array"))
          }
          .map { fields =>
            TermBankEntry(
              term = fields(0).asString.map(_.trim).getOrElse(throw new Exception("Term is empty")),
              reading = fields(1).asString.map(_.trim).filter(_.nonEmpty),
              definitionTags = fields(2).asString.map(_.trim.split(' ').filter(_.nonEmpty).toSeq).getOrElse(Seq.empty),
              inflection = fields(3).asString.map(_.trim.split(' ').filter(_.nonEmpty).toSeq).getOrElse(Seq.empty),
              popularity = fields(4).asNumber.map(_.toDouble).getOrElse(0.0),
              definitions = fields(5)
                .as[Vector[Content]]
                .fold(t => throw new Exception(s"Could not decode definitions: ${fields(5)} because $t"), identity),
              sequenceNumber = fields(6).asNumber.flatMap(_.toInt).getOrElse(0),
              termTags = fields(7).asString.map(_.trim.split(' ').filter(_.nonEmpty).toSeq).getOrElse(Seq.empty)
            )
          }
      }
    )
  }

}

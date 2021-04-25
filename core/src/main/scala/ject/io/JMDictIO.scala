package ject.io

import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.GZIPInputStream

import ject.entity.WordDocument
import zio._
import zio.blocking.Blocking
import zio.console._
import zio.stream.{ ZSink, ZStream, ZTransducer }

import scala.xml.XML

object JMDictIO {

  /** Downloads the latest JMDict file (with English definitions only) and extracts it. */
  def download(destination: Path): ZIO[Blocking with Console, Throwable, Long] = {
    val url = new URL("http://ftp.edrdg.org/pub/Nihongo/JMdict_e.gz")

    ZManaged.fromAutoCloseable(UIO(new GZIPInputStream(url.openStream()))).use { stream =>
      putStrLn(s"Downloading and extracting JMDict file to $destination") *>
        ZStream.fromInputStream(stream).run(ZSink.fromFile(destination))
    }
  }

  /**
   * Normalizes the JMDict file. This is mainly done as a workaround to prevent unwanted XML entity expansions,
   * particularly for tags and parts of speech.
   */
  def normalize(input: Path, output: Path): ZIO[Blocking with Console, Throwable, Long] = {
    val entityRegex = """<!ENTITY (\S+) "(.+?)">""".r

    putStrLn(s"Normalizing JMDict file to $output") *>
      ZStream
        .fromFile(input)
        .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
        .mapConcatChunk { line =>
          Chunk.fromArray(
            entityRegex.replaceFirstIn(s"$line\n", """<!ENTITY $1 "$1">""").getBytes(StandardCharsets.UTF_8)
          )
        }
        .run(ZSink.fromFile(output))
  }

  def load(file: Path): ZStream[Any, Throwable, WordDocument] =
    ZStream.fromIteratorEffect {
      System.setProperty("jdk.xml.entityExpansionLimit", "0")

      for {
        xml       <- Task(XML.loadFile(file.toFile))
        entryNodes = xml \ "entry"
      } yield entryNodes.iterator.map { n =>
        WordDocument(
          (n \ "ent_seq").map(_.text).head,
          (n \ "k_ele" \ "keb").map(_.text),
          (n \ "r_ele" \ "reb").map(_.text),
          (n \ "sense").map(s => (s \ "gloss").map(_.text).mkString("; ")),
          (n \ "sense" \ "dial").map(_.text).distinct,
          (n \ "sense" \ "pos").map(_.text).distinct
        )
      }
    }

}

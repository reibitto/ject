package ject.tools.jmdict

import ject.ja.docs.WordDoc
import zio.*
import zio.stream.{ZPipeline, ZSink, ZStream}
import zio.Console.printLine

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import javax.xml.parsers.SAXParserFactory
import scala.xml.{Elem, SAXParser}
import scala.xml.factory.XMLLoader

object JMDictIO {

  private lazy val xmlLoader: XMLLoader[Elem] = new XMLLoader[Elem] {

    override def parser: SAXParser = {
      val parser = SAXParserFactory.newInstance()
      parser.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true)
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
      parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
      parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
      parser.setFeature("http://xml.org/sax/features/external-general-entities", false)
      parser.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false)
      parser.setXIncludeAware(false)
      parser.setNamespaceAware(false)
      parser.newSAXParser()
    }
  }

  /** Downloads the latest JMDict file (with English definitions only) and
    * extracts it.
    */
  def download(destination: Path): ZIO[Any, Throwable, Long] = {
    val url = new URI("http://ftp.edrdg.org/pub/Nihongo/JMdict_e.gz").toURL

    ZIO.scoped {
      for {
        stream <- ZIO.fromAutoCloseable(ZIO.succeed(new GZIPInputStream(url.openStream())))
        _      <- printLine(s"Downloading and extracting JMDict file to $destination")
        length <- ZStream.fromInputStream(stream).run(ZSink.fromPath(destination))
      } yield length
    }
  }

  /** Normalizes the JMDict file. This is mainly done as a workaround to prevent
    * unwanted XML entity expansions, particularly for tags and parts of speech.
    */
  def normalize(input: Path, output: Path): ZIO[Any, Throwable, Long] = {
    val entityRegex = """<!ENTITY (\S+) "(.+?)">""".r

    printLine(s"Normalizing JMDict file to $output") *>
      ZStream
        .fromPath(input)
        .via(ZPipeline.utf8Decode)
        .via(ZPipeline.splitLines)
        .mapConcatChunk { line =>
          Chunk.fromArray(
            entityRegex.replaceFirstIn(s"$line\n", """<!ENTITY $1 "$1">""").getBytes(StandardCharsets.UTF_8)
          )
        }
        .run(ZSink.fromPath(output))
  }

  def load(file: Path): ZStream[Any, Throwable, WordDoc] =
    ZStream.fromIteratorZIO {
      java.lang.System.setProperty("jdk.xml.entityExpansionLimit", "0")

      for {
        xml <- ZIO.attempt(xmlLoader.loadFile(file.toFile))
        entryNodes = xml \ "entry"
      } yield entryNodes.iterator.map { n =>
        WordDoc(
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

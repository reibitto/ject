package ject.ja.io

import ject.ja.docs.KanjiDoc
import ject.ja.entity.Radical
import zio.{RIO, ZIO}
import zio.stream.{ZSink, ZStream}
import zio.Console.printLine

import java.net.URL
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import javax.xml.parsers.SAXParserFactory
import scala.xml.{Elem, SAXParser}
import scala.xml.factory.XMLLoader

object KanjidicIO {

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

  /** Downloads the latest kanjidic file and extracts it. */
  def download(destination: Path): RIO[Any, Long] = {
    val url = new URL("http://www.edrdg.org/kanjidic/kanjidic2.xml.gz")

    ZIO.scoped {
      for {
        stream <- ZIO.fromAutoCloseable(ZIO.succeed(new GZIPInputStream(url.openStream())))
        _      <- printLine(s"Downloading and extracting kanjidic file to $destination")
        length <- ZStream.fromInputStream(stream).run(ZSink.fromPath(destination))
      } yield length
    }
  }

  def load(file: Path, radicals: Map[String, Radical]): ZStream[Any, Throwable, KanjiDoc] =
    ZStream.fromIteratorZIO {
      for {
        xml <- ZIO.attempt(xmlLoader.loadFile(file.toFile))
        characterNodes = xml \ "character"
      } yield characterNodes.iterator.map { n =>
        val kanji = (n \ "literal").text

        KanjiDoc(
          kanji = kanji,
          meaning = (n \ "reading_meaning" \ "rmgroup" \ "meaning").filter(_.attribute("m_lang").isEmpty).map(_.text),
          onYomi = (n \ "reading_meaning" \ "rmgroup" \ "reading")
            .filter(_.attribute("r_type").exists(_.text == "ja_on"))
            .map(_.text),
          kunYomi = (n \ "reading_meaning" \ "rmgroup" \ "reading")
            .filter(_.attribute("r_type").exists(_.text == "ja_kun"))
            .map(_.text),
          nanori = (n \ "reading_meaning" \ "nanori").map(_.text),
          koreanReadings = (n \ "reading_meaning" \ "rmgroup" \ "reading")
            .filter(_.attribute("r_type").exists(_.text == "korean_h"))
            .map(_.text),
          radicalId = (n \ "radical" \ "rad_value")
            .find(_.attribute("rad_type").exists(_.text == "classical"))
            .map(_.text)
            .get
            .toInt,
          parts = radicals.values.filter(_.kanji.contains(kanji)).map(_.radical).toSeq,
          strokeCount = (n \ "misc" \ "stroke_count").map(_.text.toInt),
          frequency = (n \ "misc" \ "freq").headOption.map(_.text.toInt),
          jlpt = (n \ "misc" \ "jlpt").headOption.map(_.text.toInt),
          grade = (n \ "misc" \ "grade").headOption.map(_.text.toInt)
        )
      }
    }
}

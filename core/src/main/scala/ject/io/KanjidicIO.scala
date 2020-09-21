package ject.io

import java.io.File
import java.net.URL
import java.nio.file.Path
import java.util.zip.GZIPInputStream

import ject.entity.{ KanjiDocument, Radical }
import zio.blocking.Blocking
import zio.{ Task, UIO, ZIO, ZManaged }
import zio.console.{ putStrLn, Console }
import zio.stream.{ ZSink, ZStream }

import scala.xml.XML

object KanjidicIO {

  /** Downloads the latest kanjidic file and extracts it. */
  def download(destination: Path): ZIO[Blocking with Console, Throwable, Long] = {
    val url = new URL("http://www.edrdg.org/kanjidic/kanjidic2.xml.gz")

    ZManaged.fromAutoCloseable(UIO(new GZIPInputStream(url.openStream()))).use { stream =>
      putStrLn(s"Downloading and extracting kanjidic file to $destination") *>
        ZStream.fromInputStream(stream).run(ZSink.fromFile(destination))
    }
  }

  def load(file: Path, radicals: Map[String, Radical]): ZStream[Any, Throwable, KanjiDocument] =
    ZStream.fromIteratorEffect {
      for {
        xml           <- Task(XML.loadFile(file.toFile))
        characterNodes = xml \ "character"
      } yield characterNodes.iterator.map { n =>
        val kanji = (n \ "literal").text

        KanjiDocument(
          kanji,
          (n \ "reading_meaning" \ "rmgroup" \ "meaning").filter(_.attribute("m_lang").isEmpty).map(_.text),
          (n \ "reading_meaning" \ "rmgroup" \ "reading")
            .filter(_.attribute("r_type").exists(_.text == "ja_on"))
            .map(_.text),
          (n \ "reading_meaning" \ "rmgroup" \ "reading")
            .filter(_.attribute("r_type").exists(_.text == "ja_kun"))
            .map(_.text),
          (n \ "reading_meaning" \ "nanori").map(_.text),
          (n \ "radical" \ "rad_value")
            .find(_.attribute("rad_type").exists(_.text == "classical"))
            .map(_.text)
            .get
            .toInt,
          radicals.values.filter(_.kanji.contains(kanji)).map(_.radical).toSeq,
          (n \ "misc" \ "stroke_count").map(_.text.toInt),
          (n \ "misc" \ "frequency").headOption.map(_.text.toInt),
          (n \ "misc" \ "jlpt").headOption.map(_.text.toInt),
          (n \ "misc" \ "grade").headOption.map(_.text.toInt)
        )
      }
    }
}

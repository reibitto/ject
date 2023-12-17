package ject.tools.yomichan

import cats.syntax.functor.*
import io.circe.*
import io.circe.syntax.*

sealed trait ContentNode {
  def asText: String
}

object ContentNode {

  final case class Text(text: String) extends ContentNode {
    def asText: String = text
  }

  final case class ImageTag(tag: String, path: String) extends ContentNode {
    def asText: String = ""
  }

  final case class LinkTag(tag: String, href: String, content: Vector[ContentNode]) extends ContentNode {
    def asText: String = content.map(_.asText).mkString
  }

  final case class ContainerTag(tag: String, content: Vector[ContentNode]) extends ContentNode {

    def asText: String = {
      val text = content.map(_.asText).mkString

      tag.toLowerCase match {
        case "div" => s"\n$text"
        case _     => s"$text"
      }
    }
  }

  final case class EmptyTag(tag: String) extends ContentNode {

    def asText: String = tag.toLowerCase match {
      case "br" => "\n"
      case _    => ""
    }
  }

  implicit val decoder: Decoder[ContentNode] =
    Decoder[Text].widen[ContentNode] or
      Decoder[ImageTag].widen[ContentNode] or
      Decoder[LinkTag].widen[ContentNode] or
      Decoder[ContainerTag].widen[ContentNode] or
      Decoder[EmptyTag].widen[ContentNode]

  implicit val encoder: Encoder[ContentNode] = Encoder.instance {
    case a: Text         => a.asJson
    case a: ImageTag     => a.asJson
    case a: LinkTag      => a.asJson
    case a: ContainerTag => a.asJson
    case a: EmptyTag     => a.asJson
  }

  object Text {

    implicit val decoder: Decoder[Text] =
      Decoder.decodeString.map(Text(_))

    implicit val encoder: Encoder[Text] =
      Encoder.encodeString.contramap(_.text)
  }

  object ImageTag {

    implicit val decoder: Decoder[ImageTag] =
      Decoder.forProduct2("tag", "path")(ImageTag.apply)

    implicit val encoder: Encoder[ImageTag] =
      Encoder.forProduct2("tag", "path")(a => (a.tag, a.path))
  }

  object LinkTag {

    implicit val decoder: Decoder[LinkTag] = Decoder.instance { c =>
      for {
        tag  <- c.get[String]("tag")
        href <- c.get[String]("href")
        content <- c.get[Vector[ContentNode]]("content")
                     .orElse(
                       c.get[ContentNode]("content").map(Vector(_))
                     )
      } yield LinkTag(tag, href, content)
    }

    implicit val encoder: Encoder[LinkTag] =
      Encoder.forProduct3("tag", "href", "content")(a => (a.tag, a.href, a.content))
  }

  object ContainerTag {

    implicit val decoder: Decoder[ContainerTag] = Decoder.instance { c =>
      for {
        tag <- c.get[String]("tag")
        content <- c.get[Vector[ContentNode]]("content")
                     .orElse(
                       c.get[ContentNode]("content").map(Vector(_))
                     )
      } yield ContainerTag(tag, content)
    }

    implicit val encoder: Encoder[ContainerTag] =
      Encoder.forProduct2("tag", "content")(a => (a.tag, a.content))
  }

  object EmptyTag {

    implicit val decoder: Decoder[EmptyTag] =
      Decoder.forProduct1("tag")(EmptyTag.apply)

    implicit val encoder: Encoder[EmptyTag] =
      Encoder.forProduct1("tag")(a => a.tag)
  }

}

final case class StructuredContent(content: Vector[ContentNode])

object StructuredContent {

  implicit lazy val decoder: Decoder[StructuredContent] =
    Decoder.forProduct1("content")(StructuredContent.apply)
}

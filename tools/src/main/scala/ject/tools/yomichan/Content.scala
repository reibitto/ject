package ject.tools.yomichan

import io.circe.*

sealed trait Content {
  def asText: String
}

object Content {

  final case class Text(text: String) extends Content {
    def asText: String = text
  }

  final case class Nodes(nodes: Vector[ContentNode]) extends Content {
    def asText: String = nodes.map(_.asText).mkString
  }

  implicit val decoder: Decoder[Content] = Decoder.instance { c =>
    c.as[String] match {
      case Right(s) => Right(Content.Text(s))
      case Left(_) =>
        c.downField("type").as[String].flatMap {
          case "structured-content" =>
            // According to the term bank schema, can be either an array or object.
            c.downField("content")
              .as[Vector[ContentNode]]
              .map(Content.Nodes.apply)
              .orElse(
                c.downField("content").as[ContentNode].map(n => Content.Nodes(Vector(n)))
              )

          case other =>
            Left(
              DecodingFailure(
                DecodingFailure.Reason.CustomReason(s"Currently unsupported content type: ${other}"),
                c.downField("type")
              )
            )
        }
    }
  }
}

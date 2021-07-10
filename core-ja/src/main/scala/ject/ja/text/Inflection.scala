package ject.ja.text

import ject.ja.text.Form
import ject.ja.text.inflection.Godan
import ject.ja.text.inflection.Ichidan
import zio.NonEmptyChunk

object Inflection {
  def inflect(word: String, wordType: WordType, targetForm: Form): Either[String, NonEmptyChunk[String]] = {
    val inflections = wordType match {
      case WordType.VerbIchidan => Ichidan.inflections
      case WordType.VerbGodan   => Godan.inflections
      case wordType             => throw new NotImplementedError(s"Inflections for $wordType not yet supported.")
    }

    for {
      transforms <- inflections.get(targetForm).toRight(s"Invalid inflection form: $targetForm")
      result     <- transforms(word)
    } yield result
  }
}

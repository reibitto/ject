package ject.text.ja

import ject.text.ja.inflection.{ Godan, Ichidan }

object Inflection {
  def inflect(word: String, wordType: WordType, targetForm: Form): Either[String, String] = {
    val inflections = wordType match {
      case WordType.VerbIchidan => Ichidan.inflections
      case WordType.VerbGodan   => Godan.inflections
      case wordType             => throw new NotImplementedError(s"Inflections for $wordType not yet supported.")
    }

    for {
      transforms <- inflections.get(targetForm).toRight(s"Invalid inflection form: $targetForm")
      result     <- transforms.foldLeft(Right(word): Either[String, String]) { case (acc, transformFn) =>
                      acc.flatMap(transformFn)
                    }
    } yield result
  }
}

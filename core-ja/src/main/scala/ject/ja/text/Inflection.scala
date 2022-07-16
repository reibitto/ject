package ject.ja.text

import ject.ja.text.inflection.*
import zio.NonEmptyChunk

object Inflection {

  def inflect(word: String, wordType: WordType, targetForm: Form): Either[String, NonEmptyChunk[String]] = {
    val inflections = wordType match {
      case WordType.VerbIchidan => Ichidan.inflections
      case WordType.VerbGodan   => Godan.inflections
      case WordType.VerbSuru    => Suru.inflections
      case WordType.VerbAru     => Aru.inflections
      case WordType.VerbIku     => Iku.inflections
      case WordType.AdjectiveI  => AdjectiveI.inflections
      case WordType.AdjectiveNa => throw new NotImplementedError(s"Inflections for $wordType not yet supported.")
    }

    for {
      transforms <- inflections.get(targetForm).toRight(s"Invalid inflection form: $targetForm")
      result     <- transforms(word)
    } yield result
  }

  def inflectAll(word: String, wordType: WordType): Map[Form, Either[String, NonEmptyChunk[String]]] = {
    val inflections = wordType match {
      case WordType.VerbIchidan => Ichidan.inflections
      case WordType.VerbGodan   => Godan.inflections
      case WordType.VerbSuru    => Suru.inflections
      case WordType.VerbAru     => Aru.inflections
      case WordType.VerbIku     => Iku.inflections
      case WordType.AdjectiveI  => AdjectiveI.inflections
      case WordType.AdjectiveNa => throw new NotImplementedError(s"Inflections for $wordType not yet supported.")
    }

    inflections.map { case (k, f) =>
      (k, f(word))
    }
  }
}

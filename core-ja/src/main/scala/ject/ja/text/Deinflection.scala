package ject.ja.text

import ject.ja.text.inflection.Godan
import ject.ja.text.inflection.Ichidan
import zio.NonEmptyChunk

object Deinflection {

  def deinflect(word: String): Map[(Form, WordType), NonEmptyChunk[String]] = {
    val wordTypes: Seq[WordType] = Seq(WordType.VerbIchidan, WordType.VerbGodan)

    wordTypes.map { wordType =>
      deinflect(word, wordType).map { case (k, v) => (k, wordType) -> v }
    }.reduce(_ ++ _)
  }

  def deinflect(word: String, wordType: WordType): Map[Form, NonEmptyChunk[String]] = {
    val deinflections = wordType match {
      case WordType.VerbIchidan => Ichidan.deinflections
      case WordType.VerbGodan   => Godan.deinflections
      case wordType             => throw new NotImplementedError(s"Inflections for $wordType not yet supported.")
    }

    deinflections.map { case (form, transform) =>
      transform(word).map(form -> _)
    }.collect { case Right(v) => v }.toMap
  }
}

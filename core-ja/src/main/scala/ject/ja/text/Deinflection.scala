package ject.ja.text

import ject.ja.text.inflection.{AdjectiveI, Aru, Godan, Ichidan, Iku, Suru}
import ject.ja.text.Transformation.Transform
import zio.NonEmptyChunk

object Deinflection {

  def deinflect(word: String): Map[(Form, WordType), NonEmptyChunk[String]] = {
    val wordTypes: Seq[WordType] = Seq(WordType.VerbIchidan, WordType.VerbGodan)

    wordTypes.map { wordType =>
      deinflect(word, wordType).map { case (k, v) => (k, wordType) -> v }
    }.reduce(_ ++ _)
  }

  def deinflect(word: String, wordType: WordType): Map[Form, NonEmptyChunk[String]] = {
    val deinflections = deinflectionsFor(wordType)

    deinflections.map { case (form, transform) =>
      transform(word).map(form -> _)
    }.collect { case Right(v) => v }.toMap
  }

  def deinflect(word: String, wordType: WordType, targetForm: Form): Option[NonEmptyChunk[String]] = {
    val deinflections = deinflectionsFor(wordType)

    deinflections.get(targetForm).flatMap { transform =>
      transform(word).toOption
    }
  }

  private def deinflectionsFor(wordType: WordType): Map[Form, Transform] =
    wordType match {
      case WordType.VerbIchidan => Ichidan.deinflections
      case WordType.VerbGodan   => Godan.deinflections
      case WordType.VerbSuru    => Suru.deinflections
      case WordType.VerbAru     => Aru.deinflections
      case WordType.VerbIku     => Iku.deinflections
      case WordType.AdjectiveI  => AdjectiveI.deinflections
      case wordType @ WordType.AdjectiveNa =>
        throw new NotImplementedError(s"Deinflections for $wordType not yet supported.")
    }
}

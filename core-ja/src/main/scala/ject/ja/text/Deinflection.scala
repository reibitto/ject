package ject.ja.text

import ject.ja.text.inflection.{AdjectiveI, Aru, Godan, Ichidan, Iku, Suru}
import ject.ja.text.Transformation.Transform
import ject.ja.JapaneseText
import zio.NonEmptyChunk

object Deinflection {

  // Deinflection candidates are generated purely by string manipulation, with no dictionary to confirm they're
  // real words, so this filters out results that certainly can't be valid Japanese (see JapaneseText.isPlausibleWord).
  private def filterPlausible(candidates: NonEmptyChunk[String]): Option[NonEmptyChunk[String]] =
    NonEmptyChunk.fromChunk(candidates.filter(JapaneseText.isPlausibleWord))

  def deinflect(word: String): Map[(Form, WordType), NonEmptyChunk[String]] = {
    val wordTypes: Seq[WordType] = Seq(WordType.VerbIchidan, WordType.VerbGodan)

    wordTypes.map { wordType =>
      deinflect(word, wordType).map { case (k, v) => (k, wordType) -> v }
    }.reduce(_ ++ _)
  }

  def deinflect(word: String, wordType: WordType): Map[Form, NonEmptyChunk[String]] = {
    val deinflections = deinflectionsFor(wordType)

    deinflections.flatMap { case (form, transform) =>
      transform(word).toOption.flatMap(filterPlausible).map(form -> _)
    }
  }

  def deinflect(word: String, wordType: WordType, targetForm: Form): Option[NonEmptyChunk[String]] = {
    val deinflections = deinflectionsFor(wordType)

    deinflections.get(targetForm).flatMap { transform =>
      transform(word).toOption.flatMap(filterPlausible)
    }
  }

  private def deinflectionsFor(wordType: WordType): Map[Form, Transform] =
    wordType match {
      case WordType.VerbIchidan            => Ichidan.deinflections
      case WordType.VerbGodan              => Godan.deinflections
      case WordType.VerbSuru               => Suru.deinflections
      case WordType.VerbAru                => Aru.deinflections
      case WordType.VerbIku                => Iku.deinflections
      case WordType.AdjectiveI             => AdjectiveI.deinflections
      case wordType @ WordType.AdjectiveNa =>
        throw new NotImplementedError(s"Deinflections for $wordType not yet supported.")
    }
}

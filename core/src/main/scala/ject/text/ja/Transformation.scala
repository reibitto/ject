package ject.text.ja

import ject.locale.JapaneseText
import ject.text.ja.Syllabary.Dan
import zio.{ Chunk, NonEmptyChunk }

object Transformation {
  type Transform = String => Either[String, NonEmptyChunk[String]]

  def Transforms(transforms: Transform*): Transform = { (s: String) =>
    transforms.headOption match {
      case None                => Right(NonEmptyChunk.single(s))
      case Some(headTransform) =>
        transforms.tail.foldLeft(headTransform(s)) { case (acc, f) =>
          acc.flatMap(b => multiParam(f)(b))
        }
    }
  }

  def multiParam(
    k: String => Either[String, NonEmptyChunk[String]]
  ): Chunk[String] => Either[String, NonEmptyChunk[String]] = { (params: Chunk[String]) =>
    val collected = params.flatMap { p =>
      k(p).fold(_ => Chunk.empty, _.toChunk)
    }

    NonEmptyChunk
      .fromChunk(collected)
      .fold(Left("No candidates found"): Either[String, NonEmptyChunk[String]])(Right(_))
  }

  def ichidanStem: Transform = {
    case s if !s.endsWith("る") => Left("Verb must end in る")
    case s if s.length < 2     => Left("Verb must be greater than 1 character")
    case s                     => Right(NonEmptyChunk.single(s.init))
  }

  def godanStem: Transform = { s =>
    if (s.length < 2) {
      Left("Verb must be greater than 1 character")
    } else {
      s.last match {
        case 'く' | 'ぐ'       => Right(NonEmptyChunk.single(s"${s.init}い"))
        case 'る' | 'う' | 'つ' => Right(NonEmptyChunk.single(s"${s.init}っ"))
        case 'む' | 'ぬ' | 'ぶ' => Right(NonEmptyChunk.single(s"${s.init}ん"))
        case 'す'             => Right(NonEmptyChunk.single(s"${s.init}し"))
        case _               => Left(s"$s is not a godan verb")
      }
    }
  }

  def changeBase(dan: Syllabary.Dan, suffix: String): Transform = { s =>
    if (s.length < 2) {
      Left("Verb must be greater than 1 character")
    } else {
      val stem = s.init
      val last = s.last

      if (dan == Dan.A && last == 'う')
        Right(NonEmptyChunk.single(s"${stem}わ"))
      else {
        for {
          shifted <- Syllabary.shift(last, dan).toRight(s"Unable to shift '$last' to $dan")
        } yield NonEmptyChunk.single(s"$stem$shifted$suffix")
      }
    }
  }

  def shiftBase(fromDan: Syllabary.Dan, toDan: Syllabary.Dan): Transform = { s =>
    if (s.length < 2) {
      Left("Verb must be greater than 1 character")
    } else {
      val stem = s.init
      val last = s.last

      for {
        _             <- Syllabary.danOf(last).toRight(s"${s} must end with ${fromDan}")
        shiftedSuffix <- Syllabary.shift(last, toDan).toRight(s"Could not shift '${last}' to ${toDan}")
      } yield NonEmptyChunk.single(s"${stem}${shiftedSuffix}")
    }
  }

  def attach(suffix: String): Transform = { s =>
    Right(NonEmptyChunk.single(s + suffix))
  }

  def attachGodanStem(detachSuffixes: String*): Transform = { s =>
    if (s.length < 3) {
      Left("Verb must be greater than 2 character")
    } else {
      detachSuffixes.find(ds => s.endsWith(ds)) match {
        case None         => Left(s"$s is not a godan verb")
        case Some(suffix) =>
          val stem = s.substring(0, s.length - suffix.length - 1)

          if (s.endsWith(s"い$suffix")) {
            if (JapaneseText.hasDakuten(suffix.head))
              Right(NonEmptyChunk(s"${stem}ぐ"))
            else
              Right(NonEmptyChunk(s"${stem}く"))
          } else if (s.endsWith(s"っ$suffix")) Right(NonEmptyChunk(s"${stem}う", s"${stem}つ", s"${stem}る"))
          else if (s.endsWith(s"ん$suffix")) Right(NonEmptyChunk(s"${stem}む", s"${stem}ぬ", s"${stem}ぶ"))
          else if (s.endsWith(s"し$suffix")) Right(NonEmptyChunk(s"${stem}す"))
          else Left(s"$s is not a godan verb")
      }

    }
  }

  def ensureValidVerbEnding: Transform =
    ensureSuffix("ぶ", "ぐ", "く", "む", "る", "す", "つ", "う", "ぬ")

  def detach(suffixes: String*): Transform = { s =>
    suffixes.find(suffix => s.endsWith(suffix)) match {
      case Some(suffix) => Right(NonEmptyChunk.single(s.substring(0, s.length - suffix.length)))
      case None         => Left(s"Must end with one of the following suffixes: ${suffixes.mkString(", ")}")
    }
  }

  def ensureSuffix(suffixes: String*): Transform = { s =>
    if (suffixes.exists(suffix => s.endsWith(suffix))) {
      Right(NonEmptyChunk.single(s))
    } else {
      Left(s"Must end with one of the following suffixes: ${suffixes.mkString(", ")}")
    }
  }
}

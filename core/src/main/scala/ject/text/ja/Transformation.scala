package ject.text.ja

import ject.text.ja.Syllabary.Dan

object Transformation {
  type Transform  = String => Either[String, String]

  // TODO: Use newtypes here. Also the success output should probably be NonEmptyList since multiple valid inflections are possible
  type Transforms = Seq[String => Either[String, String]]

  def ichidanStem: Transform = {
    case s if !s.endsWith("る") => Left("Verb must end in る")
    case s if s.length < 2     => Left("Verb must be greater than 1 character")
    case s                     => Right(s.init)
  }

  def godanStem: Transform = { s =>
    if (s.length < 2) {
      Left("Verb must be greater than 1 character")
    } else {
      s.last match {
        case 'く' | 'ぐ'       => Right(s"${s.init}い")
        case 'る' | 'う' | 'つ' => Right(s"${s.init}っ")
        case 'む' | 'ぬ' | 'ぶ' => Right(s"${s.init}ん")
        case 'す'             => Right(s"${s.init}し")
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
        Right(s"${stem}わ")
      else {
        for {
          shifted <- Syllabary.shift(last, dan).toRight(s"Unable to shift '$last' to $dan")
        } yield s"$stem$shifted$suffix"
      }
    }
  }

  def attach(suffix: String): Transform = { s =>
    Right(s + suffix)
  }
}

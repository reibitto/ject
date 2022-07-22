package ject.ja.text

import enumeratum.*
import ject.ja.JapaneseText

object Syllabary {

  sealed trait Gyo extends EnumEntry

  object Gyo extends Enum[Gyo] {
    case object A extends Gyo
    case object Ka extends Gyo
    case object Sa extends Gyo
    case object Ta extends Gyo
    case object Na extends Gyo
    case object Ha extends Gyo
    case object Ma extends Gyo
    case object Ya extends Gyo
    case object Ra extends Gyo
    case object Wa extends Gyo
    case object Ga extends Gyo
    case object Za extends Gyo
    case object Da extends Gyo
    case object Ba extends Gyo
    case object Pa extends Gyo

    val values: IndexedSeq[Gyo] = findValues
  }

  sealed trait Dan extends EnumEntry

  object Dan extends Enum[Dan] {
    case object A extends Dan
    case object I extends Dan
    case object U extends Dan
    case object E extends Dan
    case object O extends Dan

    val values: IndexedSeq[Dan] = findValues
  }

  private val table: Seq[String] = Seq(
    // Hiragana
    "あいうえお",
    "かきくけこ",
    "さしすせそ",
    "たちつてと",
    "なにぬねの",
    "はひふへほ",
    "まみむめも",
    "や_ゆ_よ",
    "らりるれろ",
    "わゐ_ゑを",
    "がぎぐげご",
    "ざじずぜぞ",
    "だぢづでど",
    "ばびぶべぼ",
    "ぱぴぷぺぽ",
    // Katakana
    "アイウエオ",
    "カキクケコ",
    "サシスセソ",
    "タチツテト",
    "ナニヌネノ",
    "ハヒフヘホ",
    "マミムメモ",
    "ヤ_ユ_ヨ",
    "ラリルレロ",
    "ワヰ_ヱヲ",
    "ガギグゲゴ",
    "ザジズゼゾ",
    "ダヂヅデド",
    "バビブベボ",
    "パピプペポ"
  )

  private val indices: Map[Char, (Dan, Gyo)] =
    (for {
      (line, i) <- table.zipWithIndex
      (c, j)    <- line.zipWithIndex
    } yield c -> (Dan.values(j), Gyo.values(i % Gyo.values.length))).toMap

  def apply(c: Char): Option[(Dan, Gyo)] = indices.get(c)

  def shift(c: Char, gyo: Gyo): Option[Char] =
    for {
      (dan, _) <- apply(c)
      g        <- Gyo.valuesToIndex.get(gyo)
      d        <- Dan.valuesToIndex.get(dan)
      katakanaOffset = if (JapaneseText.isHiragana(c)) 0 else Gyo.values.length
    } yield table(g + katakanaOffset)(d)

  def shift(c: Char, dan: Dan): Option[Char] =
    for {
      (_, gyo) <- apply(c)
      g        <- Gyo.valuesToIndex.get(gyo)
      d        <- Dan.valuesToIndex.get(dan)
      katakanaOffset = if (JapaneseText.isHiragana(c)) 0 else Gyo.values.length
    } yield table(g + katakanaOffset)(d)

  def danOf(c: Char): Option[Dan] =
    indices.get(c).map(_._1)

  def gyoOf(c: Char): Option[Gyo] =
    indices.get(c).map(_._2)
}

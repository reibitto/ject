package ject.ja

import zio.*
import zio.test.*

object JapaneseTextSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("JapaneseText")(
      test("convert to hiragana") {
        assertTrue(
          JapaneseText.toHiragana("ニホンゴ") == "にほんご",
          JapaneseText.toHiragana("消しごむ") == "消しごむ",
          JapaneseText.toHiragana("けしゴム") == "けしごむ"
        )
      },
      test("convert to hiragana expands the katakana long vowel mark based on the preceding vowel") {
        assertTrue(
          JapaneseText.toHiragana("ピーチクパーチク") == "ぴいちくぱあちく",
          JapaneseText.toHiragana("コーヒー") == "こおひい",
          JapaneseText.toHiragana("ケーキ") == "けえき",
          JapaneseText.toHiragana("ラーメン") == "らあめん"
        )
      },
      test("convert to hiragana leaves a leading or otherwise unresolvable ー untouched") {
        assertTrue(JapaneseText.toHiragana("ー") == "ー")
      },
      test("convert to katakana") {
        assertTrue(
          JapaneseText.toKatakana("にほんご") == "ニホンゴ",
          JapaneseText.toKatakana("消しごむ") == "消シゴム",
          JapaneseText.toKatakana("けしゴム") == "ケシゴム"
        )
      }
    )
}

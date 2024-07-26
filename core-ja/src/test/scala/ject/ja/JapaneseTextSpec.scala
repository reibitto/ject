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
      test("convert to katakana") {
        assertTrue(
          JapaneseText.toKatakana("にほんご") == "ニホンゴ",
          JapaneseText.toKatakana("消しごむ") == "消シゴム",
          JapaneseText.toKatakana("けしゴム") == "ケシゴム"
        )
      }
    )
}

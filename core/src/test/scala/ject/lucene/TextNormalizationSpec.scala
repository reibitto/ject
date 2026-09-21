package ject.lucene

import ject.lucene.AnalyzerExtensions.*
import zio.*
import zio.test.*

object TextNormalizationSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("TextNormalization")(
      test("folds full-width digits and letters to half-width") {
        assertTrue(
          TextNormalization.normalizeWidth("４日") == "4日",
          TextNormalization.normalizeWidth("ａｂｃ") == "abc",
          TextNormalization.normalizeWidth("２０２４年") == "2024年"
        )
      },
      test("folds full-width punctuation to half-width") {
        assertTrue(
          TextNormalization.normalizeWidth("？") == "?",
          TextNormalization.normalizeWidth("＊") == "*",
          TextNormalization.normalizeWidth("～") == "~"
        )
      },
      test("leaves kanji, hiragana, and full-width katakana untouched") {
        assertTrue(
          TextNormalization.normalizeWidth("日本語") == "日本語",
          TextNormalization.normalizeWidth("ひらがな") == "ひらがな",
          TextNormalization.normalizeWidth("カタカナ") == "カタカナ"
        )
      },
      test("leaves already-half-width text unchanged, including mixed-script slang") {
        assertTrue(
          TextNormalization.normalizeWidth("4日") == "4日",
          TextNormalization.normalizeWidth("うpする") == "うpする"
        )
      },
      test("widthNormalizingAnalyzer indexes a value as exactly one width-folded term") {
        // KeywordTokenizer never splits its input, so a multi-character exact-match term (e.g. a dictionary
        // entry) still produces a single term after CJKWidthFilter folds it, not multiple tokens.
        assertTrue(
          TextNormalization.widthNormalizingAnalyzer.tokensFor("４日") == Seq("4日"),
          TextNormalization.widthNormalizingAnalyzer.tokensFor("食べる") == Seq("食べる")
        )
      }
    )
}

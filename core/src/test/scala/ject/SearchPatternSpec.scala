package ject

import zio.*
import zio.test.*

object SearchPatternSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("SearchPattern")(
      test("plain text is a Default pattern") {
        assertTrue(SearchPattern("食べる") == SearchPattern.Default("食べる"))
      },
      test("quoted text is an Exact pattern") {
        assertTrue(SearchPattern("\"食べる\"") == SearchPattern.Exact("食べる"))
      },
      test("trailing * or ~ is a Prefix pattern") {
        assertTrue(
          SearchPattern("食べ*") == SearchPattern.Prefix("食べ"),
          SearchPattern("食べ~") == SearchPattern.Prefix("食べ")
        )
      },
      test("? or a leading ~ is a Wildcard pattern") {
        assertTrue(
          SearchPattern("食べ?") == SearchPattern.Wildcard("食べ?"),
          SearchPattern("~食べる") == SearchPattern.Wildcard("~食べる")
        )
      },
      test("backtick-quoted text is a Raw pattern") {
        assertTrue(SearchPattern("`+食べる -食べない`") == SearchPattern.Raw("+食べる -食べない"))
      },
      test("full-width digits are folded to half-width, so full/half-width queries agree") {
        assertTrue(
          SearchPattern("４日") == SearchPattern.Default("4日"),
          SearchPattern("4日") == SearchPattern.Default("4日")
        )
      },
      test("width folding applies inside Exact, Prefix, Wildcard, and Raw patterns too") {
        assertTrue(
          SearchPattern("\"４日\"") == SearchPattern.Exact("4日"),
          SearchPattern("４日＊") == SearchPattern.Prefix("4日"),
          SearchPattern("４日？") == SearchPattern.Wildcard("4日?"),
          SearchPattern("｀４日｀") == SearchPattern.Raw("4日")
        )
      },
      test("mixed-script slang words are preserved, not treated as non-Japanese noise") {
        assertTrue(SearchPattern("うpする") == SearchPattern.Default("うpする"))
      }
    )
}

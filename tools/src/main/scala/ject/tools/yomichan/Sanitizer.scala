package ject.tools.yomichan

import ject.utils.StringExtensions.*

object Sanitizer {

  def sanitize(text: String): String =
    text.normalizeNewlines.replaceAll("\n{3,}", "\n\n").trim

  def sanitizeForDictionary(dicitonaryId: String)(text: String) =
    dicitonaryId match {
      case "pixiv" => sanitizePixiv(text)
      case _       => sanitize(text)
    }

  def sanitizePixiv(text: String): String =
    text
      .replace("pixivで読む", "")
      .normalizeNewlines
      .replaceAll("\n{3,}", "\n\n")
      .trim

}

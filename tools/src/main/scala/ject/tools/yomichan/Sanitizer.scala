package ject.tools.yomichan

import ject.utils.StringExtensions.*

object Sanitizer {

  def sanitize(text: String): String =
    text.normalizeNewlines.replaceAll("\n{3,}", "\n\n").trim

  // TODO:: Take in dictionary ADT (pixiv, etc)
  def sanitizeSpecific(text: String): String =
    text
      .replace("pixivで読む", "")
      .normalizeNewlines
      .replaceAll("\n{3,}", "\n\n")
      .trim

}

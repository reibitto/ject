package ject

import ject.lucene.TextNormalization
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil

sealed trait SearchPattern {
  def text: String

  /** The "raw" search pattern string. */
  def patternText: String
}

object SearchPattern {

  final case class Default(text: String) extends SearchPattern {
    def patternText: String = s"$text"
  }

  final case class Exact(text: String) extends SearchPattern {
    def patternText: String = s""""$text""""
  }

  final case class Prefix(text: String) extends SearchPattern {
    def patternText: String = s"$text*"
  }

  final case class Wildcard(text: String) extends SearchPattern {
    def patternText: String = text
  }

  final case class Raw(text: String) extends SearchPattern {
    def patternText: String = s"$text"
  }

  def apply(searchText: String): SearchPattern = {
    import ject.utils.StringExtensions.*

    // Folds full-width digits/letters/punctuation (e.g. "４日" -> "4日", "？" -> "?") to their standard-width
    // equivalents, so search text matches regardless of which width the user (or the dictionary source) used.
    val text = TextNormalization.normalizeWidth(searchText.trim)

    if (text.length >= 2 && text.isSurroundedWith("\""))
      Exact(QueryParserUtil.escape(text.tail.init))
    else if (text.startsWith("\""))
      Exact(QueryParserUtil.escape(text.tail))
    else if (text.length >= 2 && text.isSurroundedWith("`"))
      Raw(text.tail.init)
    else if (text.startsWith("`"))
      Raw(text.tail)
    else if (text.endsWith("*") || text.endsWith("~"))
      Prefix(QueryParserUtil.escape(text.init))
    else if (text.contains("?") || text.contains("*") || text.startsWith("~"))
      Wildcard(text)
    else
      Default(QueryParserUtil.escape(text))
  }
}

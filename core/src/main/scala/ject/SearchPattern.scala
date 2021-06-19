package ject

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
    import ject.utils.StringExtensions._

    val text                 = searchText.trim
    val normalizedSearchText = text.replace("？", "?").replace("＊", "*").replace("～", "~").replace("｀", "`")

    if (text.isSurroundedWith("\""))
      Exact(QueryParserUtil.escape(text.tail.init))
    else if (text.startsWith("\""))
      Exact(QueryParserUtil.escape(text.tail))
    else if (normalizedSearchText.isSurroundedWith("`"))
      Raw(normalizedSearchText.tail.init)
    else if (normalizedSearchText.startsWith("`"))
      Raw(normalizedSearchText.tail)
    else if (normalizedSearchText.endsWith("*") || normalizedSearchText.endsWith("~"))
      Prefix(QueryParserUtil.escape(normalizedSearchText.init))
    else if (
      normalizedSearchText.contains("?") || normalizedSearchText.contains("*") ||
      normalizedSearchText.startsWith("~")
    )
      Wildcard(normalizedSearchText)
    else
      Default(QueryParserUtil.escape(normalizedSearchText))
  }
}

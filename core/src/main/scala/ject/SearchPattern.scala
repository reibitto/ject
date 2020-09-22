package ject

import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil

sealed trait SearchPattern {
  def text: String

  /** The "raw" search pattern string. */
  def patternText: String
}

object SearchPattern {
  final case class Unspecified(text: String) extends SearchPattern {
    def patternText: String = s"$text"
  }

  final case class Exact(text: String) extends SearchPattern {
    def patternText: String = s""""$text""""
  }

  final case class Contains(text: String) extends SearchPattern {
    def patternText: String = s"*$text*"
  }

  final case class Prefix(text: String) extends SearchPattern {
    def patternText: String = s"$text*"
  }

  final case class Suffix(text: String) extends SearchPattern {
    def patternText: String = s"*$text"
  }

  final case class Raw(text: String) extends SearchPattern {
    def patternText: String = s"$text"
  }

  def apply(searchText: String): SearchPattern = {
    val text = searchText.trim

    if (text.startsWith("\"") && text.endsWith("\""))
      Exact(QueryParserUtil.escape(text.tail.init))
    else if (text.startsWith("\""))
      Exact(QueryParserUtil.escape(text.tail))
    if (text.startsWith("`") && text.endsWith("`"))
      Raw(text.tail.init)
    else if (text.startsWith("`"))
      Raw(text.tail)
    else if ((text.startsWith("*") && text.endsWith("*")) || (text.startsWith("~") && text.endsWith("~")))
      Contains(QueryParserUtil.escape(text.tail.init))
    else if (text.startsWith("*") || text.startsWith("~"))
      Suffix(QueryParserUtil.escape(text.tail))
    else if (text.endsWith("*") || text.endsWith("~"))
      Prefix(QueryParserUtil.escape(text.init))
    else
      Unspecified(QueryParserUtil.escape(text))
  }
}

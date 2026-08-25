package ject.ja.lucene

import ject.lucene.AnalyzerExtensions.*
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.core.KeywordTokenizer
import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.analysis.Analyzer

object JapaneseAnalyzers {
  lazy val japanese: JapaneseAnalyzer = new JapaneseAnalyzer()

  /** An analyzer for exact-match term fields (via `TextField`, not
    * `StringField` — see `ject.lucene.TextNormalization` for why) that fold
    * width and kana script differences into one canonical term:
    * `KeywordTokenizer` (never splits its input, preserving exact-match
    * semantics) -> `CJKWidthFilter` (full/half-width folding) ->
    * `KanaNormalizingFilter` (katakana -> hiragana, long vowel mark aware).
    *
    * Using this instead of manually generating and indexing/searching
    * hiragana/katakana/width variants means the term dictionary only ever holds
    * one entry per distinct word, and a query only has to be folded the same
    * way to find it.
    */
  lazy val kanaNormalizing: Analyzer = new Analyzer {
    override def createComponents(fieldName: String): Analyzer.TokenStreamComponents = {
      val tokenizer = new KeywordTokenizer()
      val widthFolded = new CJKWidthFilter(tokenizer)
      val kanaFolded = new KanaNormalizingFilter(widthFolded)
      new Analyzer.TokenStreamComponents(tokenizer, kanaFolded)
    }
  }

  /** Applies the same width+kana folding as `kanaNormalizing` to a single
    * string outside of an indexing context. Needed because `TermQuery`,
    * `PrefixQuery`, and `WildcardQuery` compare against the raw term bytes
    * directly and never run a field's analyzer on the query text — so query
    * text has to be folded explicitly to match terms indexed via
    * `kanaNormalizing`.
    */
  def normalize(text: String): String =
    kanaNormalizing.tokensFor(text).headOption.getOrElse(text)
}

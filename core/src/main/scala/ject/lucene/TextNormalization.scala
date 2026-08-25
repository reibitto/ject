package ject.lucene

import ject.lucene.AnalyzerExtensions.*
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.core.KeywordTokenizer
import org.apache.lucene.analysis.Analyzer

object TextNormalization {

  /** An analyzer that treats its whole input as a single term (like a
    * `StringField` would) but folds full-width ASCII (e.g. "４日" -> "4日", "／" ->
    * "/") and half-width katakana to their standard-width equivalents via
    * Lucene's `CJKWidthFilter` first. Kanji, hiragana, and regular full-width
    * katakana pass through untouched.
    *
    * Use this (via a `TextField`, not a `StringField` — `StringField` is never
    * tokenized, so it would ignore this analyzer entirely) for term fields that
    * need exact-match semantics but shouldn't depend on which width a
    * dictionary source or a search query happens to use. Because
    * `KeywordTokenizer` never splits its input, indexing still produces exactly
    * one term per value — normalizing the term itself this way means a query
    * only has to be folded the same way to match it, with no need to index
    * multiple width variants of the same value.
    */
  val widthNormalizingAnalyzer: Analyzer = new Analyzer {
    override def createComponents(fieldName: String): Analyzer.TokenStreamComponents = {
      val tokenizer = new KeywordTokenizer()
      new Analyzer.TokenStreamComponents(tokenizer, new CJKWidthFilter(tokenizer))
    }
  }

  /** Applies the same width folding as `widthNormalizingAnalyzer` to a single
    * string outside of an indexing context, e.g. to normalize search query text
    * so it matches terms indexed via that analyzer.
    */
  def normalizeWidth(text: String): String =
    widthNormalizingAnalyzer.tokensFor(text).headOption.getOrElse(text)
}

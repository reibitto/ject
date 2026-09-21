package ject.lucene

import ject.lucene.AnalyzerExtensions.*
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.core.KeywordTokenizer
import org.apache.lucene.analysis.Analyzer

object TextNormalization {

  /** Treats its whole input as a single term, but first folds full-width ASCII
    * (e.g. "４日" -> "4日") and half-width katakana to standard width. Kanji,
    * hiragana, and full-width katakana pass through untouched.
    *
    * Use this for exact-match term fields that shouldn't depend on which width
    * a dictionary source or query happens to use. Requires a `TextField`;
    * `StringField` is never tokenized and would ignore the analyzer.
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

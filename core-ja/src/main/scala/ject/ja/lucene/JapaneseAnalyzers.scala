package ject.ja.lucene

import ject.lucene.AnalyzerExtensions.*
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.core.KeywordTokenizer
import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.analysis.Analyzer

object JapaneseAnalyzers {
  lazy val japanese: JapaneseAnalyzer = new JapaneseAnalyzer()

  /** Folds width and kana script differences into one canonical term:
    * `KeywordTokenizer` -> `CJKWidthFilter` -> `KanaNormalizingFilter`
    * (katakana -> hiragana, long vowel mark aware). For exact-match term fields
    * declared as `TextField` (see `ject.lucene.TextNormalization` for why).
    *
    * Keeps one term per distinct word instead of indexing every
    * hiragana/katakana/width variant.
    */
  lazy val kanaNormalizing: Analyzer = new Analyzer {
    override def createComponents(fieldName: String): Analyzer.TokenStreamComponents = {
      val tokenizer = new KeywordTokenizer()
      val widthFolded = new CJKWidthFilter(tokenizer)
      val kanaFolded = new KanaNormalizingFilter(widthFolded)
      new Analyzer.TokenStreamComponents(tokenizer, kanaFolded)
    }
  }

  /** Applies the same folding as `kanaNormalizing` to a single string outside
    * of an indexing context. `TermQuery`, `PrefixQuery`, and `WildcardQuery`
    * compare raw term bytes and never run a field's analyzer, so query text has
    * to be folded explicitly.
    */
  def normalize(text: String): String =
    kanaNormalizing.tokensFor(text).headOption.getOrElse(text)
}

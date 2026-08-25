package ject.ja.lucene

import ject.ja.JapaneseText
import org.apache.lucene.analysis.{TokenFilter, TokenStream}
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

/** Folds katakana to hiragana (expanding the katakana long vowel mark ー along
  * the way, via `JapaneseText.toHiragana`) so that a term is indexed/matched
  * the same way regardless of which kana script it's written in. Kanji and
  * anything already hiragana pass through unchanged.
  */
final class KanaNormalizingFilter(input: TokenStream) extends TokenFilter(input) {
  private val termAttr: CharTermAttribute = addAttribute(classOf[CharTermAttribute])

  override def incrementToken(): Boolean =
    if (input.incrementToken()) {
      val normalized = JapaneseText.toHiragana(termAttr.toString)
      termAttr.setEmpty()
      termAttr.append(normalized)
      true
    } else
      false
}

package ject.ja

object JapaneseText {

  def isHiragana(c: Char): Boolean =
    c match {
      case code if code >= 0x3041 && code <= 0x3094   => true
      case 0x309b | 0x309c | 0x30fc | 0x30fd | 0x30fe => true
      case _                                          => false
    }

  def isHalfWidthKatakana(c: Char): Boolean = c >= 0xff65 && c <= 0xff9f

  def isFullWidthKatakana(c: Char): Boolean =
    c match {
      case code if code >= 0x30a1 && code <= 0x30fb   => true // Katakana
      case code if code >= 0x31f0 && code <= 0x31ff   => true // Phonetic Extensions for Ainu
      case 0x309b | 0x309c | 0x30fc | 0x30fd | 0x30fe => true
      case _                                          => false
    }

  def isKatakana(c: Char): Boolean = isHalfWidthKatakana(c) || isFullWidthKatakana(c)

  def isKana(c: Char): Boolean =
    (c >= 0x3041 && c <= 0x3094) || // Hiragana (without punctuation/symbols because it's included in the `isKatakana` check)
      isKatakana(c)

  def isKanji(c: Char): Boolean = c >= 0x4e00 && c <= 0x9fcc

  def isJapanese(c: Char): Boolean = isKana(c) || isKanji(c)

  // Small kana that can never legally start a word in standard Japanese orthography (they only ever modify
  // the preceding mora, e.g. きゃ, かった). A word beginning with one of these is never valid.
  private val invalidWordInitialKana: Set[Char] =
    Set(
      'ぁ', 'ぃ', 'ぅ', 'ぇ', 'ぉ', 'っ', 'ゃ', 'ゅ', 'ょ', 'ゎ', 'ァ', 'ィ', 'ゥ', 'ェ', 'ォ', 'ッ', 'ャ', 'ュ', 'ョ', 'ヮ'
    )

  /** A cheap, dictionary-free sanity check for whether `word` could plausibly
    * be a real Japanese word: every character must be kana or kanji, and it
    * must not begin with a kana that can only ever modify a preceding mora.
    * This can't confirm a word actually exists (that requires a dictionary
    * lookup), but it can cheaply reject strings that certainly aren't valid,
    * e.g. output containing stray Latin characters or digits from malformed
    * input, or a reconstruction bug that left a dangling small-tsu at the front
    * of a word.
    */
  def isPlausibleWord(word: String): Boolean =
    word.nonEmpty && word.forall(isJapanese) && !invalidWordInitialKana.contains(word.head)

  def hasDakuten(c: Char): Boolean =
    c match {
      case 'が' | 'ぎ' | 'ぐ' | 'げ' | 'ご' | 'ざ' | 'じ' | 'ず' | 'ぜ' | 'ぞ' | 'だ' | 'ぢ' | 'づ' | 'で' | 'ど' | 'ば' | 'び' | 'ぶ' |
          'べ' | 'ぼ' =>
        true
      case _ => false
    }

  def toHiragana(c: Char): Char =
    c match {
      case c if c >= 0x30a1 && c <= 0x30f3 => (c - 96).toChar
      case 'ヵ'                             => 'か'
      case 'ヶ'                             => 'け'
      case 'ヴ'                             => 'ゔ'
      case c                               => c
    }

  def toHiragana(s: String): String =
    s.map(toHiragana)

  def toKatakana(c: Char): Char =
    c match {
      case c if c >= 0x3041 && c <= 0x3093 => (c + 96).toChar
      case c                               => c
    }

  def toKatakana(s: String): String =
    s.map(toKatakana)
}

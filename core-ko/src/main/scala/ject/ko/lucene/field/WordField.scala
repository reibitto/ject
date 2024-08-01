package ject.ko.lucene.field

import enumeratum.Enum
import ject.ja.lucene.JapaneseAnalyzers
import ject.ko.lucene.KoreanAnalyzers
import ject.lucene.field.LuceneField
import ject.lucene.Analyzers
import org.apache.lucene.analysis.Analyzer

sealed abstract class WordField(val analyzer: Analyzer) extends LuceneField

object WordField extends Enum[WordField] {
  case object Id extends WordField(Analyzers.standard)

  case object HangulTerm extends WordField(KoreanAnalyzers.korean)
  case object HangulTermAnalyzed extends WordField(KoreanAnalyzers.korean)

  case object HanjaTerm extends WordField(Analyzers.standard)
  case object HanjaTermAnalyzed extends WordField(Analyzers.standard)

  case object Pronunciation extends WordField(Analyzers.standard)

  case object DefinitionEnglish extends WordField(Analyzers.english)
  case object DefinitionEnglishOther extends WordField(Analyzers.standard)

  case object DefinitionJapanese extends WordField(JapaneseAnalyzers.japanese)
  case object DefinitionJapaneseOther extends WordField(Analyzers.standard)

  case object DefinitionKorean extends WordField(KoreanAnalyzers.korean)
  case object DefinitionKoreanOther extends WordField(Analyzers.standard)

  case object Tags extends WordField(Analyzers.standard)

  // TODO: Add frequencyClass (star count)

  case object PartOfSpeech extends WordField(Analyzers.standard)

  val values: IndexedSeq[WordField] = findValues
}

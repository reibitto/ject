package ject.ja.lucene.field

import enumeratum.Enum
import ject.ja.lucene.JapaneseAnalyzers
import ject.lucene.field.LuceneField
import ject.lucene.Analyzers
import org.apache.lucene.analysis.Analyzer

sealed abstract class WordField(val analyzer: Analyzer) extends LuceneField

object WordField extends Enum[WordField] {
  case object Id extends WordField(Analyzers.standard)

  case object KanjiTerm extends WordField(Analyzers.standard)
  case object KanjiTermAnalyzed extends WordField(JapaneseAnalyzers.japanese)
  case object KanjiTermInflected extends WordField(Analyzers.standard)

  case object ReadingTerm extends WordField(Analyzers.standard)
  case object ReadingTermAnalyzed extends WordField(JapaneseAnalyzers.japanese)
  case object ReadingTermInflected extends WordField(Analyzers.standard)

  case object Definition extends WordField(Analyzers.english)
  case object DefinitionOther extends WordField(Analyzers.standard)

  case object Tags extends WordField(Analyzers.standard)

  case object PartOfSpeech extends WordField(Analyzers.standard)

  case object Priority extends WordField(Analyzers.standard)
  case object Frequency extends WordField(Analyzers.standard)

  val values: IndexedSeq[WordField] = findValues
}

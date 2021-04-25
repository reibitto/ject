package ject.lucene.field

import enumeratum.Enum

sealed trait WordField extends LuceneField

object WordField extends Enum[WordField] {
  case object Id           extends WordField
  case object KanjiTerm    extends WordField
  case object ReadingTerm  extends WordField
  case object Definition   extends WordField
  case object Tags         extends WordField
  case object PartOfSpeech extends WordField
  case object KanjiTermFuzzy   extends WordField
  case object ReadingTermFuzzy extends WordField
  case object DefinitionOther  extends WordField

  lazy val values: IndexedSeq[WordField] = findValues
}

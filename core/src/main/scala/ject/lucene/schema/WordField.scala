package ject.lucene.schema

import enumeratum.Enum
import ject.entity.WordDocument
import org.apache.lucene.document.Document

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

object WordSchema {
  def from(document: Document): WordDocument =
    WordDocument(
      document.get(WordField.Id.entryName),
      document.getValues(WordField.KanjiTerm.entryName).toIndexedSeq,
      document.getValues(WordField.ReadingTerm.entryName).toIndexedSeq,
      document.getValues(WordField.Definition.entryName).toIndexedSeq,
      document.getValues(WordField.Tags.entryName).toIndexedSeq,
      document.getValues(WordField.PartOfSpeech.entryName).toIndexedSeq
    )
}

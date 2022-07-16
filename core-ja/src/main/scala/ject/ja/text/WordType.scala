package ject.ja.text

import enumeratum.*

sealed trait WordType extends EnumEntry

object WordType extends Enum[WordType] {
  case object VerbIchidan extends WordType
  case object VerbGodan extends WordType
  case object VerbSuru extends WordType
  case object VerbAru extends WordType
  case object VerbIku extends WordType
  case object AdjectiveI extends WordType
  case object AdjectiveNa extends WordType

  val values: IndexedSeq[WordType] = findValues
}

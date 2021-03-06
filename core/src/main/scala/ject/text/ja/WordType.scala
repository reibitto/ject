package ject.text.ja

import enumeratum._

sealed trait WordType extends EnumEntry

object WordType extends Enum[WordType] {
  case object VerbIchidan extends WordType
  case object VerbGodan   extends WordType
  case object AdjectiveI  extends WordType
  case object AdjectiveNa extends WordType

  val values: IndexedSeq[WordType] = findValues
}

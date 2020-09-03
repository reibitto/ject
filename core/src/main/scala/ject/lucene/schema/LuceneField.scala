package ject.lucene.schema

import enumeratum.EnumEntry
import enumeratum.EnumEntry.LowerCamelcase

trait LuceneField extends EnumEntry with LowerCamelcase

object LuceneField {
  val none: LuceneField = new LuceneField {
    override val entryName: String = ""
  }
}

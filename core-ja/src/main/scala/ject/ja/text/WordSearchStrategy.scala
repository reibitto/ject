package ject.ja.text

import enumeratum.*

/** Controls how inflected surface forms (e.g. 話した for 話す) are matched at search
  * time.
  */
sealed trait WordSearchStrategy extends EnumEntry

object WordSearchStrategy extends Enum[WordSearchStrategy] {

  /** Every inflected form of every conjugable entry is generated at index time
    * and stored in a dedicated field, so an inflected query matches directly
    * via a term query. Larger index, no query-time ambiguity to resolve.
    */
  case object IndexInflections extends WordSearchStrategy

  /** No inflected forms are indexed. A query is deinflected back to candidate
    * dictionary forms at search time and looked up against the much smaller
    * dictionary-form fields. Smaller index, but one query fans out into several
    * lookups, and a wrong candidate can collide with an unrelated real word.
    */
  case object DeinflectQuery extends WordSearchStrategy

  val values: IndexedSeq[WordSearchStrategy] = findValues
}

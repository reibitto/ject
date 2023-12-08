package ject.tools.yomichan

/** @param term
  *   The text for the term.
  * @param reading
  *   Reading of the term.
  * @param definitionTags
  *   String of space-separated tags for the definition.
  * @param inflection
  *   String of space-separated rule identifiers for the definition which is
  *   used to validate deinflection. Valid rule identifiers are: v1: ichidan
  *   verb; v5: godan verb; vs: suru verb; vk: kuru verb; adj-i: i-adjective. An
  *   empty string corresponds to words which aren't inflected, such as nouns.
  * @param popularity
  *   Score used to determine popularity. Negative values are more rare and
  *   positive values are more frequent. This score is also used to sort search
  *   results.
  * @param definitions
  *   Array of definitions for the term.
  * @param sequenceNumber
  *   Sequence number for the term. Terms with the same sequence number can be
  *   shown together when the "resultOutputMode" option is set to "merge".
  * @param termTags
  *   String of space-separated tags for the term. An empty string is treated as
  *   no tags.
  */
final case class TermBankEntry(
    term: String,
    reading: Option[String],
    definitionTags: Seq[String],
    inflection: Seq[String],
    popularity: Double,
    definitions: Seq[String],
    sequenceNumber: Int,
    termTags: Seq[String]
)

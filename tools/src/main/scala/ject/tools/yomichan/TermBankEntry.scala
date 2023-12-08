package ject.tools.yomichan

final case class TermBankEntry(
    /** The text for the term. */
    term: String,
    /** Reading of the term. */
    reading: Option[String],
    /** String of space-separated tags for the definition. */
    definitionTags: Seq[String],
    /** String of space-separated rule identifiers for the definition which is
      * used to validate deinflection. Valid rule identifiers are: v1: ichidan
      * verb; v5: godan verb; vs: suru verb; vk: kuru verb; adj-i: i-adjective.
      * An empty string corresponds to words which aren't inflected, such as
      * nouns.
      */
    inflection: Seq[String],
    /** Score used to determine popularity. Negative values are more rare and
      * positive values are more frequent. This score is also used to sort
      * search results.
      */
    popularity: Double,
    /** Array of definitions for the term. */
    definitions: Seq[String],
    /** Sequence number for the term. Terms with the same sequence number can be
      * shown together when the "resultOutputMode" option is set to "merge".
      */
    sequenceNumber: Int,
    /** String of space-separated tags for the term. An empty string is treated
      * as no tags.
      */
    termTags: Seq[String]
)

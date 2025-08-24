package ject.ja.entity

final case class Radical(
    radicalId: Int,
    radical: String,
    variants: Set[String],
    name: String,
    kanji: Set[String]
)

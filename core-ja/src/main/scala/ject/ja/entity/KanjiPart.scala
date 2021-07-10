package ject.ja.entity

final case class KanjiPart(
  character: String,
  strokeCount: Int,
  composition: KanjiComposition,
  radical: String
)

sealed trait KanjiComposition

object KanjiComposition {
  final case class Primitive(value: String)                                      extends KanjiComposition
  final case class Horizontal(left: Seq[String], right: Seq[String])             extends KanjiComposition
  final case class Vertical(top: Seq[String], bottom: Seq[String])               extends KanjiComposition
  final case class Inclusion(outside: Seq[String], inside: Seq[String])          extends KanjiComposition
  final case class VerticalRepetition(top: Seq[String], bottom: Seq[String])     extends KanjiComposition
  final case class HorizontalSurround(outside: Seq[String], middle: Seq[String]) extends KanjiComposition
  final case class Repetition3(value: Seq[String])                               extends KanjiComposition
  final case class Repetition4(value: Seq[String])                               extends KanjiComposition
  final case class VerticalHat(top: Seq[String], bottom: Seq[String])            extends KanjiComposition
  final case class Additive(left: Seq[String], right: Seq[String])               extends KanjiComposition
}

sealed trait CompositionKind

object CompositionKind {
  case object Unit extends CompositionKind
}

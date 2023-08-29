package ject.ja.text

import enumeratum.*

final case class Form(subForms: Set[SubForm], modifiers: Set[FormModifier]) {
  def polite: Form = Form(subForms, modifiers + FormModifier.Polite)
  def negative: Form = Form(subForms, modifiers + FormModifier.Negative)

  def render: String =
    (subForms.map(_.entryName) ++ modifiers.map(_.entryName)).mkString(" | ")

  def +(that: SubForm): Form = copy(subForms = subForms + that)
}

object Form {
  def of(tenseType: SubForm*): Form = Form(tenseType.toSet, Set.empty)
}

sealed trait SubForm extends EnumEntry {
  def plain: Form = Form(Set(this), Set.empty)
  def polite: Form = Form(Set(this), Set(FormModifier.Polite))
  def negative: Form = Form(Set(this), Set(FormModifier.Negative))

  def +(that: SubForm): Form = Form(Set(this, that), Set.empty)
}

object SubForm extends Enum[SubForm] {
  case object NonPast extends SubForm
  case object Past extends SubForm
  case object Te extends SubForm
  case object Conditional extends SubForm
  case object Provisional extends SubForm
  case object Potential extends SubForm
  case object Passive extends SubForm
  case object Causative extends SubForm
  case object CausativePassive extends SubForm
  case object Volitional extends SubForm
  case object Alternative extends SubForm
  case object Imperative extends SubForm
  case object Sou extends SubForm
  case object Tai extends SubForm
  case object Progressive extends SubForm
  case object Noun extends SubForm
  case object Stem extends SubForm
  case object Adverb extends SubForm
  case object Ki extends SubForm

  val values: IndexedSeq[SubForm] = findValues
}

sealed trait FormModifier extends EnumEntry

object FormModifier extends Enum[FormModifier] {
  case object Negative extends FormModifier
  case object Polite extends FormModifier

  val values: IndexedSeq[FormModifier] = findValues
}

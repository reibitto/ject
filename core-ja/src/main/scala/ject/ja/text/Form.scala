package ject.ja.text

import enumeratum._

final case class Form(subForms: Set[SubForm], modifiers: Set[FormModifier] = Set.empty) {
  def polite: Form   = Form(subForms, modifiers + FormModifier.Polite)
  def negative: Form = Form(subForms, modifiers + FormModifier.Negative)

  def render: String =
    (subForms.map(_.entryName) ++ modifiers.map(_.entryName)).mkString(" | ")

  def +(that: SubForm): Form = copy(subForms = subForms + that)
}

object Form {
  def of(tenseType: SubForm*): Form = Form(tenseType.toSet)
}

sealed trait SubForm extends EnumEntry {
  def plain: Form    = Form(Set(this))
  def polite: Form   = Form(Set(this), Set(FormModifier.Polite))
  def negative: Form = Form(Set(this), Set(FormModifier.Negative))

  def +(that: SubForm): Form = Form(Set(this, that))
}

object SubForm extends Enum[SubForm] {
  case object NonPast          extends SubForm
  case object Past             extends SubForm
  case object Te               extends SubForm
  case object Conditional      extends SubForm
  case object Provisional      extends SubForm
  case object Potential        extends SubForm
  case object Passive          extends SubForm
  case object Causative        extends SubForm
  case object CausativePassive extends SubForm
  case object Volitional       extends SubForm
  case object Alternative      extends SubForm
  case object Imperative       extends SubForm
  case object Sou              extends SubForm
  case object Tai              extends SubForm
  case object Progressive      extends SubForm

  val values: IndexedSeq[SubForm] = findValues
}

sealed trait FormModifier extends EnumEntry

object FormModifier extends Enum[FormModifier] {
  case object Negative extends FormModifier
  case object Polite   extends FormModifier

  val values: IndexedSeq[FormModifier] = findValues
}

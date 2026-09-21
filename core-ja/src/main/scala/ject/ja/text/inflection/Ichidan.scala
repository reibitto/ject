package ject.ja.text.inflection

import ject.ja.text.{ReversibleTransform, SuffixConjugator, Transforms}
import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*

object Ichidan {

  private val conjugate = SuffixConjugator(ichidanStem, "る")

  // Every ending below is declared once and yields both its forward inflection and backward deinflection,
  // since ichidan endings are always the ichidan stem (dictionary form minus る) plus a fixed suffix.
  private val conjugations: Map[Form, ReversibleTransform] = Map(
    // Plain
    Past.plain -> conjugate("た"),
    Te.plain -> conjugate("て"),
    Conditional.plain -> conjugate("たら"),
    Provisional.plain -> conjugate("れば"),
    Potential.plain -> conjugate("られる"),
    Passive.plain -> conjugate("られる"),
    Causative.plain -> conjugate("させる"),
    CausativePassive.plain -> conjugate("させられる"),
    Volitional.plain -> conjugate("よう"),
    Alternative.plain -> conjugate("たり"),
    Imperative.plain -> conjugate("ろ"),
    Sou.plain -> conjugate("そう"),
    Tai.plain -> conjugate("たい"),
    Progressive.plain -> conjugate("ている", "てる"),
    Form.of(Progressive, Past) -> conjugate("ていた", "てた"),
    Form.of(Potential, Stem) -> conjugate("られ"),
    Form.of(Potential, Te) -> conjugate("られて"),
    Form.of(Potential, Past) -> conjugate("られた"),
    Form.of(Passive, Stem) -> conjugate("られ"),
    Form.of(Passive, Te) -> conjugate("られて"),
    Form.of(Passive, Past) -> conjugate("られた"),
    Form.of(Causative, Stem) -> conjugate("させ"),
    Form.of(Causative, Te) -> conjugate("させて"),
    Form.of(Causative, Past) -> conjugate("させた"),
    Form.of(Tai, Te) -> conjugate("たくて"),
    Form.of(Stem) -> conjugate(""),
    // Polite
    NonPast.polite -> conjugate("ます"),
    Past.polite -> conjugate("ました"),
    Te.polite -> conjugate("まして"),
    Conditional.polite -> conjugate("ましたら"),
    Provisional.polite -> conjugate("ますなら"),
    Potential.polite -> conjugate("られます"),
    Passive.polite -> conjugate("られます"),
    Causative.polite -> conjugate("させます"),
    CausativePassive.polite -> conjugate("させられます"),
    Volitional.polite -> conjugate("ましょう"),
    Alternative.polite -> conjugate("ましたり"),
    Imperative.polite -> conjugate("なさい"),
    Progressive.polite -> conjugate("ています", "てます"),
    Form.of(Progressive, Past).polite -> conjugate("ていました", "てました"),
    Form.of(Potential, Te).polite -> conjugate("られまして"),
    Form.of(Potential, Past).polite -> conjugate("られました"),
    Form.of(Causative, Te).polite -> conjugate("させまして"),
    Form.of(Causative, Past).polite -> conjugate("させました"),
    // Negative
    NonPast.negative -> conjugate("ない", "ぬ", "ず"),
    Past.negative -> conjugate("なかった"),
    Te.negative -> conjugate("なくて", "ないで"),
    Conditional.negative -> conjugate("なかったら"),
    Provisional.negative -> conjugate("なければ"),
    Potential.negative -> conjugate("られない"),
    Passive.negative -> conjugate("られない"),
    Causative.negative -> conjugate("させない"),
    CausativePassive.negative -> conjugate("させられない"),
    Volitional.negative -> conjugate("まい"),
    Alternative.negative -> conjugate("なかったり"),
    Sou.negative -> conjugate("なさそう"),
    Form.of(Potential, Sou).negative -> conjugate("られなさそう"),
    Tai.negative -> conjugate("たくない"),
    Progressive.negative -> conjugate("ていない", "てない"),
    Form.of(Progressive, Past).negative -> conjugate("ていなかった", "てなかった"),
    Form.of(Tai, Sou).negative -> conjugate("たくなさそう"),
    Form.of(Potential, Te).negative -> conjugate("られなくて"),
    Form.of(Potential, Past).negative -> conjugate("られなかった"),
    Form.of(Causative, Te).negative -> conjugate("させなくて"),
    Form.of(Causative, Past).negative -> conjugate("させなかった"),
    // Polite negative
    NonPast.polite.negative -> conjugate("ません"),
    Past.polite.negative -> conjugate("ませんでした"),
    Te.polite.negative -> conjugate("ませんで"),
    Conditional.polite.negative -> conjugate("ませんでしたら"),
    Provisional.polite.negative -> conjugate("ませんなら"),
    Potential.polite.negative -> conjugate("られません"),
    Passive.polite.negative -> conjugate("られません"),
    Causative.polite.negative -> conjugate("させません"),
    CausativePassive.polite.negative -> conjugate("させられません"),
    Volitional.polite.negative -> conjugate("ますまい"),
    Alternative.polite.negative -> conjugate("ませんでしたり"),
    Imperative.polite.negative -> conjugate("なさるな"),
    Progressive.polite.negative -> conjugate("ていません", "てません"),
    Form.of(Progressive, Past).polite.negative -> conjugate("ていませんでした", "てませんでした"),
    Form.of(Potential, Te).polite.negative -> conjugate("られませんでして"),
    Form.of(Potential, Past).polite.negative -> conjugate("られませんでした"),
    Form.of(Causative, Te).polite.negative -> conjugate("させませんでして"),
    Form.of(Causative, Past).polite.negative -> conjugate("させませんでした")
  )

  // The な-prohibitive attaches to the full dictionary form (not the stem), unlike every other ending above,
  // so it can't be expressed via `conjugate` (which always reattaches the stripped `る`).
  private val imperativeNegative: ReversibleTransform =
    ReversibleTransform(
      forward = Transforms(attach("な")),
      backward = Transforms(detach("な"), ensureSuffix("る"))
    )

  val inflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity, Imperative.negative -> imperativeNegative.forward) ++
      conjugations.view.mapValues(_.forward)

  val deinflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms(ensureSuffix("る")), Imperative.negative -> imperativeNegative.backward) ++
      conjugations.view.mapValues(_.backward)
}

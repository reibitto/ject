package ject.ja.text.inflection

import ject.ja.text.{ReversibleTransform, SuffixConjugator, Transforms}
import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*

object Suru {

  private val conjugate = SuffixConjugator(suruStem, "する")

  private val conjugations: Map[Form, ReversibleTransform] = Map(
    // Plain
    Past.plain -> conjugate("した"),
    Te.plain -> conjugate("して"),
    Conditional.plain -> conjugate("したら"),
    Provisional.plain -> conjugate("すれば"),
    Potential.plain -> conjugate("できる"),
    Passive.plain -> conjugate("される"),
    Causative.plain -> conjugate("させる", "さす"),
    CausativePassive.plain -> conjugate("させられる"),
    Volitional.plain -> conjugate("しよう"),
    Alternative.plain -> conjugate("したり"),
    Imperative.plain -> conjugate("しろ"),
    Sou.plain -> conjugate("しそう"),
    Tai.plain -> conjugate("したい"),
    Form.of(Potential, Te) -> conjugate("できて"),
    Form.of(Potential, Past) -> conjugate("できた"),
    Form.of(Causative, Te) -> conjugate("させて"),
    Form.of(Causative, Past) -> conjugate("させた"),
    Form.of(Tai, Te) -> conjugate("したくて"),
    Form.of(Stem) -> conjugate("し"),
    // Polite
    NonPast.polite -> conjugate("します"),
    Past.polite -> conjugate("しました"),
    Te.polite -> conjugate("しまして"),
    Conditional.polite -> conjugate("しましたら"),
    Provisional.polite -> conjugate("しますなら"),
    Potential.polite -> conjugate("できます"),
    Passive.polite -> conjugate("されます"),
    Causative.polite -> conjugate("させます", "さします"),
    CausativePassive.polite -> conjugate("させられます"),
    Volitional.polite -> conjugate("しましょう"),
    Alternative.polite -> conjugate("しましたり"),
    Imperative.polite -> conjugate("しなさい"),
    Form.of(Potential, Te).polite -> conjugate("できまして"),
    Form.of(Potential, Past).polite -> conjugate("できました"),
    Form.of(Causative, Te).polite -> conjugate("させまして"),
    Form.of(Causative, Past).polite -> conjugate("させました"),
    // Negative
    NonPast.negative -> conjugate("しない", "せぬ", "せず"),
    Past.negative -> conjugate("しなかった"),
    Te.negative -> conjugate("しなくて", "しないで"),
    Conditional.negative -> conjugate("しなかったら"),
    Provisional.negative -> conjugate("しなければ"),
    Potential.negative -> conjugate("できない"),
    Passive.negative -> conjugate("されない"),
    Causative.negative -> conjugate("させない", "ささない"),
    CausativePassive.negative -> conjugate("させられない"),
    Volitional.negative -> conjugate("するまい"),
    Alternative.negative -> conjugate("しなかったり"),
    Imperative.negative -> conjugate("するな"),
    Sou.negative -> conjugate("しなさそう"),
    Form.of(Potential, Sou).negative -> conjugate("できなさそう"),
    Tai.negative -> conjugate("したくない"),
    Form.of(Tai, Sou).negative -> conjugate("したくなさそう"),
    Form.of(Potential, Te).negative -> conjugate("できなくて"),
    Form.of(Potential, Past).negative -> conjugate("できなかった"),
    Form.of(Causative, Te).negative -> conjugate("させなくて"),
    Form.of(Causative, Past).negative -> conjugate("させなかった"),
    // Polite negative
    NonPast.polite.negative -> conjugate("しません"),
    Past.polite.negative -> conjugate("しませんでした"),
    Te.polite.negative -> conjugate("しませんで"),
    Conditional.polite.negative -> conjugate("しませんでしたら"),
    Provisional.polite.negative -> conjugate("しませんなら"),
    Potential.polite.negative -> conjugate("できません"),
    Passive.polite.negative -> conjugate("されません"),
    Causative.polite.negative -> conjugate("させません", "さしません"),
    CausativePassive.polite.negative -> conjugate("させられません"),
    Volitional.polite.negative -> conjugate("しますまい"),
    Alternative.polite.negative -> conjugate("しませんでしたり"),
    Imperative.polite.negative -> conjugate("しなさるな"),
    Form.of(Potential, Te).polite.negative -> conjugate("できませんでして"),
    Form.of(Potential, Past).polite.negative -> conjugate("できませんでした"),
    Form.of(Causative, Te).polite.negative -> conjugate("させませんでして"),
    Form.of(Causative, Past).polite.negative -> conjugate("させませんでした")
  )

  val inflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++ conjugations.view.mapValues(_.forward)

  val deinflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++ conjugations.view.mapValues(_.backward)
}

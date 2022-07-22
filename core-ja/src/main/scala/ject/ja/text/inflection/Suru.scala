package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object Suru {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms.pure("した"),
    Te.plain -> Transforms.pure("して"),
    Conditional.plain -> Transforms.pure("したら"),
    Provisional.plain -> Transforms.pure("すれば"),
    Potential.plain -> Transforms.pure("できる"),
    Passive.plain -> Transforms.pure("される"),
    Causative.plain -> Transforms.pure("させる", "さす"),
    CausativePassive.plain -> Transforms.pure("させられる"),
    Volitional.plain -> Transforms.pure("しよう"),
    Alternative.plain -> Transforms.pure("したり"),
    Imperative.plain -> Transforms.pure("しろ"),
    Sou.plain -> Transforms.pure("しそう"),
    Tai.plain -> Transforms.pure("したい"),
    Form.of(Past, Potential) -> Transforms.pure("できた"),
    // Polite
    NonPast.polite -> Transforms.pure("します"),
    Past.polite -> Transforms.pure("しました"),
    Te.polite -> Transforms.pure("しまして"),
    Conditional.polite -> Transforms.pure("しましたら"),
    Provisional.polite -> Transforms.pure("しますなら"),
    Potential.polite -> Transforms.pure("できます"),
    Passive.polite -> Transforms.pure("されます"),
    Causative.polite -> Transforms.pure("させます", "さします"),
    CausativePassive.polite -> Transforms.pure("させられます"),
    Volitional.polite -> Transforms.pure("しましょう"),
    Alternative.polite -> Transforms.pure("しましたり"),
    Imperative.polite -> Transforms.pure("しなさい"),
    Form.of(Past, Potential).polite -> Transforms.pure("できました"),
    // Negative
    NonPast.negative -> Transforms.pure("しない", "せぬ", "せず"),
    Past.negative -> Transforms.pure("しなかった"),
    Te.negative -> Transforms.pure("しなくて"),
    Conditional.negative -> Transforms.pure("しなかったら"),
    Provisional.negative -> Transforms.pure("しなければ"),
    Potential.negative -> Transforms.pure("できない"),
    Passive.negative -> Transforms.pure("されない"),
    Causative.negative -> Transforms.pure("させない", "ささない"),
    CausativePassive.negative -> Transforms.pure("させられない"),
    Volitional.negative -> Transforms.pure("するまい"),
    Alternative.negative -> Transforms.pure("しなかったり"),
    Imperative.negative -> Transforms.pure("するな"),
    Sou.negative -> Transforms.pure("しなさそう"),
    Form.of(Potential, Sou).negative -> Transforms.pure("できなさそう"),
    Tai.negative -> Transforms.pure("したくない"),
    Form.of(Tai, Sou).negative -> Transforms.pure("したくなさそう"),
    Form.of(Past, Potential).negative -> Transforms.pure("できなかった"),
    // Polite negative
    NonPast.polite.negative -> Transforms.pure("しません"),
    Past.polite.negative -> Transforms.pure("しませんでした"),
    Te.polite.negative -> Transforms.pure("しませんで"),
    Conditional.polite.negative -> Transforms.pure("しませんでしたら"),
    Provisional.polite.negative -> Transforms.pure("しませんなら"),
    Potential.polite.negative -> Transforms.pure("できません"),
    Passive.polite.negative -> Transforms.pure("されません"),
    Causative.polite.negative -> Transforms.pure("させません", "さしません"),
    CausativePassive.polite.negative -> Transforms.pure("させられません"),
    Volitional.polite.negative -> Transforms.pure("しますまい"),
    Alternative.polite.negative -> Transforms.pure("しませんでしたり"),
    Imperative.polite.negative -> Transforms.pure("しなさるな"),
    Form.of(Past, Potential).polite.negative -> Transforms.pure("でませんでした")
  )

  val deinflections: Map[Form, Transform] = Map.empty // TODO: Implement
}

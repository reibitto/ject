package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object Suru {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(suruStem, attach("した")),
    Te.plain -> Transforms(suruStem, attach("して")),
    Conditional.plain -> Transforms(suruStem, attach("したら")),
    Provisional.plain -> Transforms(suruStem, attach("すれば")),
    Potential.plain -> Transforms(suruStem, attach("できる")),
    Passive.plain -> Transforms(suruStem, attach("される")),
    Causative.plain -> Transforms(suruStem, attach("させる", "さす")),
    CausativePassive.plain -> Transforms(suruStem, attach("させられる")),
    Volitional.plain -> Transforms(suruStem, attach("しよう")),
    Alternative.plain -> Transforms(suruStem, attach("したり")),
    Imperative.plain -> Transforms(suruStem, attach("しろ")),
    Sou.plain -> Transforms(suruStem, attach("しそう")),
    Tai.plain -> Transforms(suruStem, attach("したい")),
    Form.of(Potential, Te) -> Transforms(suruStem, attach("できて")),
    Form.of(Potential, Past) -> Transforms(suruStem, attach("できた")),
    Form.of(Tai, Te) -> Transforms(suruStem, attach("したくて")),
    // Polite
    NonPast.polite -> Transforms(suruStem, attach("します")),
    Past.polite -> Transforms(suruStem, attach("しました")),
    Te.polite -> Transforms(suruStem, attach("しまして")),
    Conditional.polite -> Transforms(suruStem, attach("しましたら")),
    Provisional.polite -> Transforms(suruStem, attach("しますなら")),
    Potential.polite -> Transforms(suruStem, attach("できます")),
    Passive.polite -> Transforms(suruStem, attach("されます")),
    Causative.polite -> Transforms(suruStem, attach("させます", "さします")),
    CausativePassive.polite -> Transforms(suruStem, attach("させられます")),
    Volitional.polite -> Transforms(suruStem, attach("しましょう")),
    Alternative.polite -> Transforms(suruStem, attach("しましたり")),
    Imperative.polite -> Transforms(suruStem, attach("しなさい")),
    Form.of(Potential, Te).polite -> Transforms(suruStem, attach("できまして")),
    Form.of(Potential, Past).polite -> Transforms(suruStem, attach("できました")),
    // Negative
    NonPast.negative -> Transforms(suruStem, attach("しない", "せぬ", "せず")),
    Past.negative -> Transforms(suruStem, attach("しなかった")),
    Te.negative -> Transforms(suruStem, attach("しなくて", "しないで")),
    Conditional.negative -> Transforms(suruStem, attach("しなかったら")),
    Provisional.negative -> Transforms(suruStem, attach("しなければ")),
    Potential.negative -> Transforms(suruStem, attach("できない")),
    Passive.negative -> Transforms(suruStem, attach("されない")),
    Causative.negative -> Transforms(suruStem, attach("させない", "ささない")),
    CausativePassive.negative -> Transforms(suruStem, attach("させられない")),
    Volitional.negative -> Transforms(suruStem, attach("するまい")),
    Alternative.negative -> Transforms(suruStem, attach("しなかったり")),
    Imperative.negative -> Transforms(suruStem, attach("するな")),
    Sou.negative -> Transforms(suruStem, attach("しなさそう")),
    Form.of(Potential, Sou).negative -> Transforms(suruStem, attach("できなさそう")),
    Tai.negative -> Transforms(suruStem, attach("したくない")),
    Form.of(Tai, Sou).negative -> Transforms(suruStem, attach("したくなさそう")),
    Form.of(Potential, Te).negative -> Transforms(suruStem, attach("できなくて")),
    Form.of(Potential, Past).negative -> Transforms(suruStem, attach("できなかった")),
    // Polite negative
    NonPast.polite.negative -> Transforms(suruStem, attach("しません")),
    Past.polite.negative -> Transforms(suruStem, attach("しませんでした")),
    Te.polite.negative -> Transforms(suruStem, attach("しませんで")),
    Conditional.polite.negative -> Transforms(suruStem, attach("しませんでしたら")),
    Provisional.polite.negative -> Transforms(suruStem, attach("しませんなら")),
    Potential.polite.negative -> Transforms(suruStem, attach("できません")),
    Passive.polite.negative -> Transforms(suruStem, attach("されません")),
    Causative.polite.negative -> Transforms(suruStem, attach("させません", "さしません")),
    CausativePassive.polite.negative -> Transforms(suruStem, attach("させられません")),
    Volitional.polite.negative -> Transforms(suruStem, attach("しますまい")),
    Alternative.polite.negative -> Transforms(suruStem, attach("しませんでしたり")),
    Imperative.polite.negative -> Transforms(suruStem, attach("しなさるな")),
    Form.of(Potential, Te).polite.negative -> Transforms(suruStem, attach("でませんでして")),
    Form.of(Potential, Past).polite.negative -> Transforms(suruStem, attach("でませんでした")),
    // Other
    Form.of(Stem) -> attach("し")
  )

  val deinflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(detach("した"), attach("する")),
    Te.plain -> Transforms(detach("して"), attach("する")),
    Conditional.plain -> Transforms(detach("したら"), attach("する")),
    Provisional.plain -> Transforms(detach("すれば"), attach("する")),
    Potential.plain -> Transforms(detach("できる"), attach("する")),
    Passive.plain -> Transforms(detach("される"), attach("する")),
    Causative.plain -> Transforms(detach("させる", "さす"), attach("する")),
    CausativePassive.plain -> Transforms(detach("させられる"), attach("する")),
    Volitional.plain -> Transforms(detach("しよう"), attach("する")),
    Alternative.plain -> Transforms(detach("したり"), attach("する")),
    Imperative.plain -> Transforms(detach("しろ"), attach("する")),
    Sou.plain -> Transforms(detach("しそう"), attach("する")),
    Tai.plain -> Transforms(detach("したい"), attach("する")),
    Form.of(Potential, Te) -> Transforms(detach("できて"), attach("する")),
    Form.of(Potential, Past) -> Transforms(detach("できた"), attach("する")),
    Form.of(Tai, Te) -> Transforms(detach("したくて"), attach("する")),
    // Polite
    NonPast.polite -> Transforms(detach("します"), attach("する")),
    Past.polite -> Transforms(detach("しました"), attach("する")),
    Te.polite -> Transforms(detach("しまして"), attach("する")),
    Conditional.polite -> Transforms(detach("しましたら"), attach("する")),
    Provisional.polite -> Transforms(detach("しますなら"), attach("する")),
    Potential.polite -> Transforms(detach("できます"), attach("する")),
    Passive.polite -> Transforms(detach("されます"), attach("する")),
    Causative.polite -> Transforms(detach("させます", "さします"), attach("する")),
    CausativePassive.polite -> Transforms(detach("させられます"), attach("する")),
    Volitional.polite -> Transforms(detach("しましょう"), attach("する")),
    Alternative.polite -> Transforms(detach("しましたり"), attach("する")),
    Imperative.polite -> Transforms(detach("しなさい"), attach("する")),
    Form.of(Potential, Te).polite -> Transforms(detach("できまして"), attach("する")),
    Form.of(Potential, Past).polite -> Transforms(detach("できました"), attach("する")),
    // Negative
    NonPast.negative -> Transforms(detach("しない", "せぬ", "せず"), attach("する")),
    Past.negative -> Transforms(detach("しなかった"), attach("する")),
    Te.negative -> Transforms(detach("しなくて", "しないで"), attach("する")),
    Conditional.negative -> Transforms(detach("しなかったら"), attach("する")),
    Provisional.negative -> Transforms(detach("しなければ"), attach("する")),
    Potential.negative -> Transforms(detach("できない"), attach("する")),
    Passive.negative -> Transforms(detach("されない"), attach("する")),
    Causative.negative -> Transforms(detach("させない", "ささない"), attach("する")),
    CausativePassive.negative -> Transforms(detach("させられない"), attach("する")),
    Volitional.negative -> Transforms(detach("するまい"), attach("する")),
    Alternative.negative -> Transforms(detach("しなかったり"), attach("する")),
    Imperative.negative -> Transforms(detach("するな"), attach("する")),
    Sou.negative -> Transforms(detach("しなさそう"), attach("する")),
    Form.of(Potential, Sou).negative -> Transforms(detach("できなさそう"), attach("する")),
    Tai.negative -> Transforms(detach("したくない"), attach("する")),
    Form.of(Tai, Sou).negative -> Transforms(detach("したくなさそう"), attach("する")),
    Form.of(Potential, Te).negative -> Transforms(detach("できなくて"), attach("する")),
    Form.of(Potential, Past).negative -> Transforms(detach("できなかった"), attach("する")),
    // Polite negative
    NonPast.polite.negative -> Transforms(detach("しません"), attach("する")),
    Past.polite.negative -> Transforms(detach("しませんでした"), attach("する")),
    Te.polite.negative -> Transforms(detach("しませんで"), attach("する")),
    Conditional.polite.negative -> Transforms(detach("しませんでしたら"), attach("する")),
    Provisional.polite.negative -> Transforms(detach("しませんなら"), attach("する")),
    Potential.polite.negative -> Transforms(detach("できません"), attach("する")),
    Passive.polite.negative -> Transforms(detach("されません"), attach("する")),
    Causative.polite.negative -> Transforms(detach("させません", "さしません"), attach("する")),
    CausativePassive.polite.negative -> Transforms(detach("させられません"), attach("する")),
    Volitional.polite.negative -> Transforms(detach("しますまい"), attach("する")),
    Alternative.polite.negative -> Transforms(detach("しませんでしたり"), attach("する")),
    Imperative.polite.negative -> Transforms(detach("しなさるな"), attach("する")),
    Form.of(Potential, Te).polite.negative -> Transforms(detach("でませんでして"), attach("する")),
    Form.of(Potential, Past).polite.negative -> Transforms(detach("でませんでした"), attach("する")),
    // Other
    Form.of(Stem) -> Transforms(detach("し"), attach("する"))
  )
}

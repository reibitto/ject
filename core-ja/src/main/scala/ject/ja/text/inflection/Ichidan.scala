package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object Ichidan {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(ichidanStem, attach("た")),
    Te.plain -> Transforms(ichidanStem, attach("て")),
    Conditional.plain -> Transforms(ichidanStem, attach("たら")),
    Provisional.plain -> Transforms(ichidanStem, attach("れば")),
    Potential.plain -> Transforms(ichidanStem, attach("られる")),
    Passive.plain -> Transforms(ichidanStem, attach("られる")),
    Causative.plain -> Transforms(ichidanStem, attach("させる")),
    CausativePassive.plain -> Transforms(ichidanStem, attach("させられる")),
    Volitional.plain -> Transforms(ichidanStem, attach("よう")),
    Alternative.plain -> Transforms(ichidanStem, attach("たり")),
    Imperative.plain -> Transforms(ichidanStem, attach("ろ")),
    Sou.plain -> Transforms(ichidanStem, attach("そう")),
    Tai.plain -> Transforms(ichidanStem, attach("たい")),
    Progressive.plain -> Transforms(ichidanStem, attach("ている", "てる")),
    Form.of(Past, Potential) -> Transforms(ichidanStem, attach("られた")),
    // Polite
    NonPast.polite -> Transforms(ichidanStem, attach("ます")),
    Past.polite -> Transforms(ichidanStem, attach("ました")),
    Te.polite -> Transforms(ichidanStem, attach("まして")),
    Conditional.polite -> Transforms(ichidanStem, attach("ましたら")),
    Provisional.polite -> Transforms(ichidanStem, attach("ますなら")),
    Potential.polite -> Transforms(ichidanStem, attach("られます")),
    Passive.polite -> Transforms(ichidanStem, attach("られます")),
    Causative.polite -> Transforms(ichidanStem, attach("させます")),
    CausativePassive.polite -> Transforms(ichidanStem, attach("させられます")),
    Volitional.polite -> Transforms(ichidanStem, attach("ましょう")),
    Alternative.polite -> Transforms(ichidanStem, attach("ましたり")),
    Imperative.polite -> Transforms(ichidanStem, attach("なさい")),
    Progressive.polite -> Transforms(ichidanStem, attach("ています", "てます")),
    Form.of(Past, Potential).polite -> Transforms(ichidanStem, attach("られました")),
    // Negative
    NonPast.negative -> Transforms(ichidanStem, attach("ない")),
    Past.negative -> Transforms(ichidanStem, attach("なかった")),
    Te.negative -> Transforms(ichidanStem, attach("なくて", "ないで")),
    Conditional.negative -> Transforms(ichidanStem, attach("なかったら")),
    Provisional.negative -> Transforms(ichidanStem, attach("なければ")),
    Potential.negative -> Transforms(ichidanStem, attach("られない")),
    Passive.negative -> Transforms(ichidanStem, attach("られない")),
    Causative.negative -> Transforms(ichidanStem, attach("させない")),
    CausativePassive.negative -> Transforms(ichidanStem, attach("させられない")),
    Volitional.negative -> Transforms(ichidanStem, attach("まい")),
    Alternative.negative -> Transforms(ichidanStem, attach("なかったり")),
    Imperative.negative -> Transforms(attach("な")),
    Sou.negative -> Transforms(ichidanStem, attach("なさそう")),
    Form.of(Potential, Sou).negative -> Transforms(ichidanStem, attach("られなさそう")),
    Tai.negative -> Transforms(ichidanStem, attach("たくない")),
    Progressive.negative -> Transforms(ichidanStem, attach("いない", "ない")),
    Form.of(Tai, Sou).negative -> Transforms(ichidanStem, attach("たくなさそう")),
    Form.of(Past, Potential).negative -> Transforms(ichidanStem, attach("られなかった")),
    // Polite negative
    NonPast.polite.negative -> Transforms(ichidanStem, attach("ません")),
    Past.polite.negative -> Transforms(ichidanStem, attach("ませんでした")),
    Te.polite.negative -> Transforms(ichidanStem, attach("ませんで")),
    Conditional.polite.negative -> Transforms(ichidanStem, attach("ませんでしたら")),
    Provisional.polite.negative -> Transforms(ichidanStem, attach("ませんなら")),
    Potential.polite.negative -> Transforms(ichidanStem, attach("られません")),
    Passive.polite.negative -> Transforms(ichidanStem, attach("られません")),
    Causative.polite.negative -> Transforms(ichidanStem, attach("させません")),
    CausativePassive.polite.negative -> Transforms(ichidanStem, attach("させられません")),
    Volitional.polite.negative -> Transforms(ichidanStem, attach("ますまい")),
    Alternative.polite.negative -> Transforms(ichidanStem, attach("ませんでしたり")),
    Imperative.polite.negative -> Transforms(ichidanStem, attach("なさるな")),
    Progressive.polite.negative -> Transforms(ichidanStem, attach("いません", "ません")),
    Form.of(Past, Potential).polite.negative -> Transforms(ichidanStem, attach("られませんでした")),
    // Other
    Form.of(Stem) -> Transforms(ichidanStem)
  )

  val deinflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms(ensureSuffix("る")),
    Past.plain -> Transforms(detach("た"), attach("る")),
    Te.plain -> Transforms(detach("て"), attach("る")),
    Conditional.plain -> Transforms(detach("たら"), attach("る")),
    Provisional.plain -> Transforms(detach("れば"), attach("る")),
    Potential.plain -> Transforms(detach("られる"), attach("る")),
    Passive.plain -> Transforms(detach("られる"), attach("る")),
    Causative.plain -> Transforms(detach("させる"), attach("る")),
    CausativePassive.plain -> Transforms(detach("させられる"), attach("る")),
    Volitional.plain -> Transforms(detach("よう"), attach("る")),
    Alternative.plain -> Transforms(detach("たり"), attach("る")),
    Imperative.plain -> Transforms(detach("ろ"), attach("る")),
    Sou.plain -> Transforms(detach("そう"), attach("る")),
    Tai.plain -> Transforms(detach("たい"), attach("る")),
    Form.of(Past, Potential) -> Transforms(detach("られた"), attach("る")),
    // Polite
    NonPast.polite -> Transforms(detach("ます"), attach("る")),
    Past.polite -> Transforms(detach("ました"), attach("る")),
    Te.polite -> Transforms(detach("まして"), attach("る")),
    Conditional.polite -> Transforms(detach("ましたら"), attach("る")),
    Provisional.polite -> Transforms(detach("ますなら"), attach("る")),
    Potential.polite -> Transforms(detach("られます"), attach("る")),
    Passive.polite -> Transforms(detach("られます"), attach("る")),
    Causative.polite -> Transforms(detach("させます"), attach("る")),
    CausativePassive.polite -> Transforms(detach("させられます"), attach("る")),
    Volitional.polite -> Transforms(detach("ましょう"), attach("る")),
    Alternative.polite -> Transforms(detach("ましたり"), attach("る")),
    Imperative.polite -> Transforms(detach("なさい"), attach("る")),
    Form.of(Past, Potential).polite -> Transforms(detach("られました"), attach("る")),
    // Negative
    NonPast.negative -> Transforms(detach("ない"), attach("る")),
    Past.negative -> Transforms(detach("なかった"), attach("る")),
    Te.negative -> Transforms(detach("なくて", "ないで"), attach("る")),
    Conditional.negative -> Transforms(detach("なかったら"), attach("る")),
    Provisional.negative -> Transforms(detach("なければ"), attach("る")),
    Potential.negative -> Transforms(detach("られない"), attach("る")),
    Passive.negative -> Transforms(detach("られない"), attach("る")),
    Causative.negative -> Transforms(detach("させない"), attach("る")),
    CausativePassive.negative -> Transforms(detach("させられない"), attach("る")),
    Volitional.negative -> Transforms(detach("まい"), attach("る")),
    Alternative.negative -> Transforms(detach("なかったり"), attach("る")),
    Imperative.negative -> Transforms(detach("な"), attach("る")),
    Sou.negative -> Transforms(detach("なさそう"), attach("る")),
    Form.of(Potential, Sou).negative -> Transforms(detach("られなさそう"), attach("る")),
    Tai.negative -> Transforms(detach("たくない"), attach("る")),
    Form.of(Tai, Sou).negative -> Transforms(detach("たくなさそう"), attach("る")),
    Form.of(Past, Potential).negative -> Transforms(detach("られなかった"), attach("る")),
    // Polite negative
    NonPast.polite.negative -> Transforms(detach("ません"), attach("る")),
    Past.polite.negative -> Transforms(detach("ませんでした"), attach("る")),
    Te.polite.negative -> Transforms(detach("ませんで"), attach("る")),
    Conditional.polite.negative -> Transforms(detach("ませんでしたら"), attach("る")),
    Provisional.polite.negative -> Transforms(detach("ませんなら"), attach("る")),
    Potential.polite.negative -> Transforms(detach("られません"), attach("る")),
    Passive.polite.negative -> Transforms(detach("られません"), attach("る")),
    Causative.polite.negative -> Transforms(detach("させません"), attach("る")),
    CausativePassive.polite.negative -> Transforms(detach("させられません"), attach("る")),
    Volitional.polite.negative -> Transforms(detach("ますまい"), attach("る")),
    Alternative.polite.negative -> Transforms(detach("ませんでしたり"), attach("る")),
    Imperative.polite.negative -> Transforms(detach("なさるな"), attach("る")),
    Form.of(Past, Potential).polite.negative -> Transforms(detach("られませんでした"), attach("る")),
    // Other
    Form.of(Stem) -> Transforms(attach("る"))
  )
}

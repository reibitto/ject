package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object Aru {

  private def aruStem: Transform = stemOf("ある")

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(aruStem, attach("あった")),
    Te.plain -> Transforms(aruStem, attach("あって")),
    Conditional.plain -> Transforms(aruStem, attach("あったら")),
    Provisional.plain -> Transforms(aruStem, attach("あれば")),
    Volitional.plain -> Transforms(aruStem, attach("あろう")),
    Alternative.plain -> Transforms(aruStem, attach("あったり")),
    Sou.plain -> Transforms(aruStem, attach("ありそう")),
    // Polite
    NonPast.polite -> Transforms(aruStem, attach("あります")),
    Past.polite -> Transforms(aruStem, attach("ありました")),
    Te.polite -> Transforms(aruStem, attach("ありまして")),
    Conditional.polite -> Transforms(aruStem, attach("ありましたら")),
    Provisional.polite -> Transforms(aruStem, attach("ありますなら")),
    Volitional.polite -> Transforms(aruStem, attach("ありましょう")),
    Alternative.polite -> Transforms(aruStem, attach("ありましたり")),
    // Negative
    NonPast.negative -> Transforms(aruStem, attach("ない")),
    Past.negative -> Transforms(aruStem, attach("なかった")),
    Te.negative -> Transforms(aruStem, attach("なくて")),
    Conditional.negative -> Transforms(aruStem, attach("なかったら")),
    Provisional.negative -> Transforms(aruStem, attach("なければ")),
    Volitional.negative -> Transforms(aruStem, attach("あるまい")),
    Alternative.negative -> Transforms(aruStem, attach("なかったり")),
    Sou.negative -> Transforms(aruStem, attach("なさそう")),
    // Polite negative
    NonPast.polite.negative -> Transforms(aruStem, attach("ありません")),
    Past.polite.negative -> Transforms(aruStem, attach("ありませんでした")),
    Te.polite.negative -> Transforms(aruStem, attach("ありませんで")),
    Conditional.polite.negative -> Transforms(aruStem, attach("ありませんでしたら")),
    Provisional.polite.negative -> Transforms(aruStem, attach("ありませんなら")),
    Volitional.polite.negative -> Transforms(aruStem, attach("ありますまい")),
    Alternative.polite.negative -> Transforms(aruStem, attach("ありませんでしたり"))
  )

  val deinflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(detach("あった"), attach("ある")),
    Te.plain -> Transforms(detach("あって"), attach("ある")),
    Conditional.plain -> Transforms(detach("あったら"), attach("ある")),
    Provisional.plain -> Transforms(detach("あれば"), attach("ある")),
    Volitional.plain -> Transforms(detach("あろう"), attach("ある")),
    Alternative.plain -> Transforms(detach("あったり"), attach("ある")),
    Sou.plain -> Transforms(detach("ありそう"), attach("ある")),
    // Polite
    NonPast.polite -> Transforms(detach("あります"), attach("ある")),
    Past.polite -> Transforms(detach("ありました"), attach("ある")),
    Te.polite -> Transforms(detach("ありまして"), attach("ある")),
    Conditional.polite -> Transforms(detach("ありましたら"), attach("ある")),
    Provisional.polite -> Transforms(detach("ありますなら"), attach("ある")),
    Volitional.polite -> Transforms(detach("ありましょう"), attach("ある")),
    Alternative.polite -> Transforms(detach("ありましたり"), attach("ある")),
    // Negative
    NonPast.negative -> Transforms(detach("ない"), attach("ある")),
    Past.negative -> Transforms(detach("なかった"), attach("ある")),
    Te.negative -> Transforms(detach("なくて"), attach("ある")),
    Conditional.negative -> Transforms(detach("なかったら"), attach("ある")),
    Provisional.negative -> Transforms(detach("なければ"), attach("ある")),
    Volitional.negative -> Transforms(detach("あるまい"), attach("ある")),
    Alternative.negative -> Transforms(detach("なかったり"), attach("ある")),
    Sou.negative -> Transforms(detach("なさそう"), attach("ある")),
    // Polite negative
    NonPast.polite.negative -> Transforms(detach("ありません"), attach("ある")),
    Past.polite.negative -> Transforms(detach("ありませんでした"), attach("ある")),
    Te.polite.negative -> Transforms(detach("ありませんで"), attach("ある")),
    Conditional.polite.negative -> Transforms(detach("ありませんでしたら"), attach("ある")),
    Provisional.polite.negative -> Transforms(detach("ありませんなら"), attach("ある")),
    Volitional.polite.negative -> Transforms(detach("ありますまい"), attach("ある")),
    Alternative.polite.negative -> Transforms(detach("ありませんでしたり"), attach("ある"))
  )
}

package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object AdjectiveI {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(adjectiveIStem, attach("かった")),
    Te.plain -> Transforms(adjectiveIStem, attach("くて")),
    Conditional.plain -> Transforms(adjectiveIStem, attach("かったら")),
    Provisional.plain -> Transforms(adjectiveIStem, attach("ければ")),
    Alternative.plain -> Transforms(adjectiveIStem, attach("かったり")),
    Sou.plain -> Transforms(adjectiveIStem, attach("そう")),
    // Negative
    NonPast.negative -> Transforms(adjectiveIStem, attach("くない")),
    Past.negative -> Transforms(adjectiveIStem, attach("くなかった")),
    Te.negative -> Transforms(adjectiveIStem, attach("くなくて")),
    Conditional.negative -> Transforms(adjectiveIStem, attach("くなかったら")),
    Provisional.negative -> Transforms(adjectiveIStem, attach("くなければ")),
    Alternative.negative -> Transforms(adjectiveIStem, attach("くなかったり")),
    Sou.negative -> Transforms(adjectiveIStem, attach("くなさそう"))
  )

  val deinflections: Map[Form, Transform] = Map.empty // TODO: Implement
}

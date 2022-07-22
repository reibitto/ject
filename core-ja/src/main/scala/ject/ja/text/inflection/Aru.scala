package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object Aru {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms.pure("あった"),
    Te.plain -> Transforms.pure("あって"),
    Conditional.plain -> Transforms.pure("あったら"),
    Provisional.plain -> Transforms.pure("あれば"),
    Volitional.plain -> Transforms.pure("あろう"),
    Alternative.plain -> Transforms.pure("あったり"),
    Sou.plain -> Transforms.pure("ありそう"),
    // Polite
    NonPast.polite -> Transforms.pure("あります"),
    Past.polite -> Transforms.pure("ありました"),
    Te.polite -> Transforms.pure("ありまして"),
    Conditional.polite -> Transforms.pure("ありましたら"),
    Provisional.polite -> Transforms.pure("ありますなら"),
    Volitional.polite -> Transforms.pure("ありましょう"),
    Alternative.polite -> Transforms.pure("ありましたり"),
    // Negative
    NonPast.negative -> Transforms.pure("ない"),
    Past.negative -> Transforms.pure("なかった"),
    Te.negative -> Transforms.pure("なくて"),
    Conditional.negative -> Transforms.pure("なかったら"),
    Provisional.negative -> Transforms.pure("なければ"),
    Volitional.negative -> Transforms.pure("あるまい"),
    Alternative.negative -> Transforms.pure("なかったり"),
    Sou.negative -> Transforms.pure("なさそう"),
    // Polite negative
    NonPast.polite.negative -> Transforms.pure("ありません"),
    Past.polite.negative -> Transforms.pure("ありませんでした"),
    Te.polite.negative -> Transforms.pure("ありませんで"),
    Conditional.polite.negative -> Transforms.pure("ありませんでしたら"),
    Provisional.polite.negative -> Transforms.pure("ありませんなら"),
    Volitional.polite.negative -> Transforms.pure("ありますまい"),
    Alternative.polite.negative -> Transforms.pure("ありませんでしたり")
  )

  val deinflections: Map[Form, Transform] = Map.empty // TODO: Implement
}
